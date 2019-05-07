package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	private JmsTemplate jmsTemplate;

	//用于单点发送 solr 导入的消息
	@Autowired
	private Destination queueSolrDestination;
	//用于单点发送 solr 删除的消息
	@Autowired
	private Destination queueSolrDeleteDestination;
	//发布网页添加
	@Autowired
	private Destination topicPageDestination;
	//发布网页删除
	@Autowired
	private Destination topicPageDeleteDestination;

	@Reference
	private GoodsService goodsService;

	/*//为了审核商品通过后导入通过的数据进solr
	@Reference
	private ItemSearchService itemSearchService;*/

/*	@Reference
	private ItemPageService itemPageService;*/
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		String name= SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getTbGoods().setSellerId(name);
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败！");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
	    //出于安全考虑，在商户后台执行的商品修改，必须要校验提交的商品属于该商户
        //校验是否是当前商家的 id
        Goods goods2 = goodsService.findOne(goods.getTbGoods().getId());
        //获取当前登录的商家 ID
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        //如果传递过来的商家 ID 并不是当前登录的用户的 ID,则属于非法操作
        if(!goods2.getTbGoods().getSellerId().equals(sellerId)
                || !goods.getTbGoods().getSellerId().equals(sellerId) ){
            return new Result(false, "操作非法");
        }


		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//商品审核被删除时存进solr的数据一起也被删除
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			//删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);
	}

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			if("1".equals(status)){
				//查询出审核通过的商品的信息
				List<TbItem> list = goodsService.findItemListByGoodsIdandStatus(ids, status);
				//将审核通过的商品的信息导入solr中
				if(list.size()>0){
					/*for (TbItem tbItem : list) {
						Map specMap= JSON.parseObject(tbItem.getSpec());//将 spec段中的 json字符串转换为 map
						tbItem.setSpecMap(specMap);//给specMap字段赋值，使得动态域获得值
					}
					itemSearchService.importList(list);*/
					final String text = JSON.toJSONString(list);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(text);
						}
					});

				}else{
					System.out.println("没有明细数据");
				}

				/*//静态页生成
				for(Long goodsId:ids){
					itemPageService.genItemHtml(goodsId);
				}*/
				for (Long id : ids) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id+"");
						}
					});
				}


			}


			return new Result(true, "审批成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "审批失败");
		}
	}
}
