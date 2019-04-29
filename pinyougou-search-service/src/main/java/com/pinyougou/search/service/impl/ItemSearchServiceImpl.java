package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout=3000)//允许最大延迟3秒加载出来，否则报错
@Transactional
public class ItemSearchServiceImpl implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;

    //主方法页：
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> resultMap=new HashMap<>();
/*
        不高亮显示的方法：
        Query query=new SimpleQuery();
        //添加查询的条件封装进 Criteria里面
        //查询的条件即为让复制域来匹配关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        List<TbItem> items = tbItems.getContent();//获得所有匹配了关键字的信息
        resultMap.put("rows",items);
        return resultMap;*/

        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        //防止商品搜索框里什么都不输报错
        if (searchMap.get("keywords").equals("")){
            return null;
        }
        //1.显示高亮查询列表：
        resultMap.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类名称：
        List<String> list = searchCategoryList(searchMap);
        resultMap.put("categoryList",list);

        //3.查询品牌和规格列表
        String category= (String) searchMap.get("category");
        if(!category.equals("")){//如果有分类名称，说明买家选了这个商品分类，需要按照卖家的要求来查询规格选项
           resultMap.putAll(searchBrandAndSpecList(category));
        }else{//如果没有分类名称，按照默认第一个查询
            if(list.size()>0){
               resultMap.putAll(searchBrandAndSpecList(list.get(0)));
            }
        }

        return resultMap;
    }

    //1.显示高亮查询列表：
    private Map<String, Object> searchList(Map searchMap){
        Map<String,Object> map=new HashMap<>();
        //使用高亮显示：
        //创建高亮查询对象：
        HighlightQuery query=new SimpleHighlightQuery();
        //高亮选项初始化：
        //设置高亮域作用让高亮显示在哪个域范围里：
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");
        //设置高亮的类型及颜色
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//将前面的封装了高亮属性的对象放入高亮查询对象里面

        //1.1 关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 按商品分类过滤
        if(!"".equals(searchMap.get("category"))  )	{//如果用户选择了分类
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按品牌过滤
        if(!"".equals(searchMap.get("brand"))  )	{//如果用户选择了品牌
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 按规格过滤
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
            for(String key :specMap.keySet()){

                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }

        }

        //1.5按价格过滤
        if(!"".equals(searchMap.get("price"))){//如果用户选择了价格
            //获得价格区间
            String[] price = ((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            if(!price[1].equals("*")){//如果区间终点不等于*
                Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页查询
        Integer pageNo = (Integer)searchMap.get("pageNo");//获得查询的页码
        if (pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize = (Integer)searchMap.get("pageSize");//获得每页显示的条数
        if (pageSize==null){
            pageSize=20;//默认每页显示20条
        }

        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);

        //1.7排序
        String sortValue = (String) searchMap.get("sort");//获取排序方式ASC DESC
        String sortField = (String) searchMap.get("sortField");//获取需要排序的域
        if (sortValue!=null&&!sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }



        //***********  获取高亮结果集  ***********
        //获取高亮页对象
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> entryList = highlightPage.getHighlighted();
        //循环遍历高亮入口集合
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表(个数和设置的高亮域个数有关)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
            /*
			for(Highlight h:highlightList){
				List<String> sns = h.getSnipplets();//每个域有可能存储多值
				System.out.println(sns);
			}*/

            if(highlightList.size()>0&&highlightList.get(0).getSnipplets().size()>0){
                TbItem entity = entry.getEntity();
                entity.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        //前面把所有高亮信息封装好以后，获得符合要求的且高亮显示的商品信集合
        List<TbItem> tbItems = highlightPage.getContent();
        map.put("rows",tbItems);
        map.put("totalPages",highlightPage.getTotalPages());//返回总的页数
        map.put("total",highlightPage.getTotalElements());//返回总记录数
        return map;
    }

    //2.根据关键字查询商品分类：
    private List<String> searchCategoryList(Map searchMap){
        List<String> list=new ArrayList<>();
        //创建查询对象
        Query query=new SimpleQuery();

        //创建查询条件对象即为让复制域来匹配关键字查询：
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //创建分组对象,设置分组选项
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //得到分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);

        //根据设置的分组域得到分组结果集
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

        //获得分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        //获得分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();

        for (GroupEntry<TbItem> entry : content) {
            //遍历集合，获得关键字下按照item_category分组后商品的分类，如输入：三星，可以获得手机或者电视这两个分类
            String groupValue = entry.getGroupValue();
            list.add(groupValue);
        }
        return list;
    }

    //3.根据category从缓存中添加品牌列表和规格列表进集合
    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //从缓存获取模板id
        Long  typeId =(Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(typeId!=null){
            //根据模板 ID 查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            // 根据模板 ID 查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

    //导入商品审核通过的数据进solr
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    //商品审核被删除时存进solr的数据一起也被删除
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品 ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
