package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    //从solr中查询出含有关键字的全部商品信息
    Map<String,Object> search(Map searchMap);


}
