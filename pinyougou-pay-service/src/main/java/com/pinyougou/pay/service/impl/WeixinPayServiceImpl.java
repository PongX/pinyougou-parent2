package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;


import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    //商户微信公众账号
    @Value("${appid}")
    private String appid;

    //商户账号
    @Value("${partner}")
    private String partner;

    //商户密钥
    @Value("${partnerkey}")
    private String partnerkey;

    //根据统一商户订单号和总金额，生成微信支付二维码
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //创建请求参数map集合
        Map<String,String> param=new HashMap<>();
        param.put("appid",appid);//商家公众号
        param.put("mch_id",partner);//商家账号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","品优购");//商品描述
        param.put("out_trade_no",out_trade_no);//商户订单号
        param.put("total_fee",total_fee);//商品总价（分）
        param.put("spbill_create_ip","127.0.0.1");//终端ip
        param.put("notify_url","http://www.itheima.com");//回调地址（随便写）
        param.put("trade_type","NATIVE");//交易类型


        try {
            //生成请求参数的xml,里面同时也自动生成了签名
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            //发送post请求
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            //返回请求结果,并封装返回
            String  result = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额(分)
            map.put("out_trade_no",out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }


    }

    //根据商品订单号循环查询订单支付状态
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map param =new HashMap();
        param.put("appid", appid);//公众账号 ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            String url="https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient client=new HttpClient(url);
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.post();

            //返回结果
            String result = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
