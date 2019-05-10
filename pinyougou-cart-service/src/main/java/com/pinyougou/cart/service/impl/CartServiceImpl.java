package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
//购物车服务实现类
@Service
@Transactional
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper tbItemMapper;

    //添加商品到购物车
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        if (tbItem==null){
            throw new RuntimeException("商品不存在！");
        }
        if (!tbItem.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效！");
        }
        //2.获取商家 ID
        String sellerId = tbItem.getSellerId();
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        //4.如果原购物车列表中不存在该商家的购物车
        if (cart==null){
            //4.1 新建购物车对象
            cart=new Cart();
            //设置商品id
            cart.setSellerId(sellerId);
            //设置商家名称
            cart.setSellerName(tbItem.getSeller());
            //设置购物车明细
            TbOrderItem orderItem = createOrderItem(tbItem, num);
            List<TbOrderItem> orderItemList=new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到原购物车列表
            cartList.add(cart);
        }else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem tbOrderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (tbOrderItem==null){
                //5.1. 如果没有，新增购物车明细
                tbOrderItem = createOrderItem(tbItem, num);
                cart.getOrderItemList().add(tbOrderItem);
            }else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                //添加数量
                tbOrderItem.setNum(tbOrderItem.getNum()+num);
                //更改金额
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum()*tbOrderItem.getPrice().doubleValue()));
                //如果数量操作后小于等于 0，则移除
                if(tbOrderItem.getNum()<=0){
                    cart.getOrderItemList().remove(tbOrderItem);//移除购物车明细
                }
                //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    // 根据商家 ID 查询购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    //根据sku商品表和商品数量设置购物车明细
    private TbOrderItem createOrderItem(TbItem item,Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    //根据orderItemList和itemId查询购物车明细列表中是否存在该商品
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId ) {
        for (TbOrderItem tbOrderItem : orderItemList) {
           if (tbOrderItem.getItemId().longValue()==itemId.longValue()){
               return tbOrderItem;
           }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    //根据登陆用户名从redis中查询购物车列表
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从缓存中查询购物车列表..."+username);
        List<Cart>cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            //相当于返回了一个null
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    //将购物车保存到 redis
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向 redis 存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);

    }

    //合并购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车...");
        for (Cart cart : cartList2) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList1=addGoodsToCartList(cartList1,tbOrderItem.getItemId(),tbOrderItem.getNum());
            }
        }

        return cartList1;
    }

}
