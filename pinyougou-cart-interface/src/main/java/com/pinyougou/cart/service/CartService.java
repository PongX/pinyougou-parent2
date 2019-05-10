package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

//购物车服务接口
public interface CartService {

    //根据原购物车的信息，商品id ，商品购买数量添加商品到购物车
  public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num );

    //根据登陆用户名从redis中查询购物车列表
    public List<Cart> findCartListFromRedis(String username);

    //将购物车保存到 redis
    public void saveCartListToRedis(String username,List<Cart> cartList);

    //合并购物车
  public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
