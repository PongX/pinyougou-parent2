package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    //添加所有商品状态为1的sku商品进solr
    public void importItemData(){
        TbItemExample example =new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核
        List<TbItem> itemList = tbItemMapper.selectByExample(example);
        System.out.println("商品列表================");
        for (TbItem tbItem : itemList) {
            Map specMap= JSON.parseObject(tbItem.getSpec());//将 spec段中的 json字符串转换为 map
            tbItem.setSpecMap(specMap);//给specMap字段赋值，使得动态域获得值
            System.out.println(tbItem.getTitle());
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("商品结束=================");
    }

    //删除所有solr中的数据
    public void deleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil= (SolrUtil) context.getBean("solrUtil");
        //添加所有
        //solrUtil.importItemData();
        //删除所有
        solrUtil.deleteAll();
    }
}
