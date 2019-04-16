package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;

public interface BrandService {
    //在一个页面查询所有信息：
    List<TbBrand> findAll();

    //分页查询：
    //PageResult findPage(int page,int size);
    //分页查询包括可以模糊查询：
    PageResult findPage(TbBrand brand,int page,int size);

    //添加一个品牌
    void add(TbBrand brand);

    //修改品牌：
    //首先查询这个品牌：
    TbBrand findOne(long id);
    //然后把修改的内容封装在TbBrand里面，传入TbBrand修改这个品牌：
    void update(TbBrand brand);

    //删除选中品牌：
    void delete(long[] ids);

}
