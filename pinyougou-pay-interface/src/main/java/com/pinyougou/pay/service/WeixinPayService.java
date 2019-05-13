package com.pinyougou.pay.service;

import java.util.Map;

//微信支付接口
public interface WeixinPayService {
    //根据统一商户订单号和总金额，生成微信支付二维码
    public Map createNative(String out_trade_no, String total_fee);

    //根据商品订单号循环查询订单支付状态
    public Map queryPayStatus(String out_trade_no);

}
