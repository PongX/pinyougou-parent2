package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到消息...");
        TextMessage textMessage=(TextMessage)message;
        try {
            String text = textMessage.getText();
            List<TbItem> items = JSON.parseArray(text, TbItem.class);
            for (TbItem item : items) {
                System.out.println(item.getId()+" "+item.getTitle());
                Map specMap= JSON.parseObject(item.getSpec());//将 spec段中的 json字符串转换为 map
                item.setSpecMap(specMap);//给specMap字段赋值，使得动态域获得值
            }
            //索引库导入
            itemSearchService.importList(items);
            System.out.println("成功导入到索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
