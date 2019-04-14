package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/brand")
@RestController//相当于controller注解和response注解的合体，response注解可以直接返回json数据回去
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")//虽然配置文件配置了*.do，但是这里可以不用写.do。
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }
}
