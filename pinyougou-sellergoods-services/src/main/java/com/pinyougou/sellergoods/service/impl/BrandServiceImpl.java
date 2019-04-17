package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;
    //在一个页面查询所有信息：
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    //分页查询包括可以模糊查询：
    @Override
    public PageResult findPage(TbBrand brand,int page, int size) {
        PageHelper.startPage(page,size);
        TbBrandExample example=new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if (brand!=null){
            if (brand.getName()!=null && brand.getName().length()>0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        Page<TbBrand> page1 = (Page<TbBrand>)brandMapper.selectByExample(example);
        return new PageResult(page1.getTotal(),page1.getResult());
    }

    //添加一个品牌：
    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    //修改品牌：
    //首先查询这个品牌：
    @Override
    public TbBrand findOne(long id) {
        return brandMapper.selectByPrimaryKey(id);
    }
    //然后把修改的内容封装在TbBrand里面，传入TbBrand修改这个品牌：
    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    //删除选中品牌：
    @Override
    public void delete(long[] ids) {
        for (long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    //用于关联模板中的品牌下拉列表
    @Override
    public List<Map> selectOptionList() {
        List<Map> maps = brandMapper.selectOptionList();
        return maps;
    }
}
