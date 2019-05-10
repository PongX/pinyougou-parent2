package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.entity.Result;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    //为避免调用远程服务超时，我们可以将过期时间改为 6 秒（默认为 1 秒）
    @Reference(timeout=6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    //查询购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
       /* 未合并cookie和Redis
       //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")){
            //用户未登陆，从cookie中获取购物车列表信息
            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            //如果cookie中没有储存购物车列表的信息，为防止空指针异常，给他赋值为[]
            if (cartListString==null||cartListString.length()==0){
                cartListString="[]";
            }
            //将json字符串变为对象
            List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
            System.out.println("空购物车在cookie中为："+cartList_cookie);
            return cartList_cookie;
        }else {
            //用户登陆，从redis中获取购物车列表信息
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            return cartList_redis;
        }*/
       //合并cookie和Redis
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //从cookie中获取购物车列表信息
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        //如果cookie中没有储存购物车列表的信息，为防止空指针异常，给他赋值为[]
        if (cartListString==null||cartListString.length()==0){
            cartListString="[]";
        }
        //将json字符串变为对象
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")) {
            //用户未登陆
            return cartList_cookie;
        }else {
            //用户登陆了，从缓存中获取购物车列表
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size()>0){
                //如果本地购物车cookie里有信息
                //合并购物车
               cartList_redis=cartService.mergeCartList(cartList_redis,cartList_cookie);
               //清除cookie中的数据
                CookieUtil.deleteCookie(request,response,"cartList");
                //清除缓存，并将购物车列表信息存入到redis中
                cartService.saveCartListToRedis(username,cartList_redis);
            }
            return cartList_redis;
        }

        }


    //添加商品到购物车
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前用户："+username);
        try {
            //从cookie或者Redis中获取原有购物车列表
            List<Cart> cartList = findCartList();
            //添加商品到购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")){
                //用户未登陆，新添加的购物车列表存到cookie中
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向 cookie 存入数据");
            }else {
                //用户登陆，新添加的购物车列表存到redis中
                cartService.saveCartListToRedis(username,cartList);
                System.out.println("向 redis 存入数据");
            }

            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }


    }



}
