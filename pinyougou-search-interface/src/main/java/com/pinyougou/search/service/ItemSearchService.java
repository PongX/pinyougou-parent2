package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    //从solr中查询出含有关键字的全部商品信息
    Map<String,Object> search(Map searchMap);

    //导入商品审核通过的数据进solr
    public void importList(List list);

    //商品审核被删除时存进solr的数据一起也被删除
    public void deleteByGoodsIds(List goodsIdList);


}
