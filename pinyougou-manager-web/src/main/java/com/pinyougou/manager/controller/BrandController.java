package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/brand")
@RestController//相当于controller注解和response注解的合体，response注解可以直接返回json数据回去
public class BrandController {

    @Reference
    private BrandService brandService;

    //在一个页面查询所有信息：
    @RequestMapping("/findAll")//虽然配置文件配置了*.do，但是这里可以不用写.do。
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

   /* //分页查询：
    @RequestMapping("/findPage")
    public PageResult findPage(int page,int size){
       return brandService.findPage(page,size);
    }*/
   //分页查询包括可以模糊查询：
   @RequestMapping("/search")
   public PageResult findPage(@RequestBody TbBrand brand, int page,int size){
       return brandService.findPage(brand,page,size);
   }

    //添加一个品牌：
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败！");
        }
    }

    //修改品牌：
    //首先查询这个品牌：
    @RequestMapping("/findOne")
    public TbBrand findOne(long id){
        return brandService.findOne(id);
    }
    //然后把修改的内容封装在TbBrand里面，传入TbBrand修改这个品牌：
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败！");
        }
    }

    //删除选中品牌：
    @RequestMapping("/delete")
    public Result delete(long[]ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败！");
        }
    }

    //用于关联模板中的品牌下拉列表
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
       return brandService.selectOptionList();
    }


}
