package com.pinyougou.content.service.impl;
import java.util.List;

import com.pinyougou.content.service.ContentService;
import com.pinyougou.entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {

		contentMapper.insert(content);
		//后台管理添加之后，清除缓存，让前台页面重新从数据库查询之后再存在缓存之中。
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//修改的时候，自身的id肯定不会变，而CategoryId可能变，所以在修改前，根据id从数据库中先查出以前的CategoryId
		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
		Long categoryId = tbContent.getCategoryId();//获得以前的CategoryId
		redisTemplate.boundHashOps("content").delete(categoryId);//先把以前的为categoryId的缓存先清除干净
		contentMapper.updateByPrimaryKey(content);//更新数据库
		//如果以前的categoryId和需要修改的categoryId不一样，说明categoryId也被修改了，所以也需要清除新的categoryId的缓存
		if (categoryId!=content.getCategoryId()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//批量删除时，必须先清除缓存，不能像add一样，因为删除了之后再也查不到对应id的categoryId了，缓存也就无法再清除
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();//广告分类 ID
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	//根据categoryId查询所有的TbContent
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		List<TbContent> list =(List<TbContent>)redisTemplate.boundHashOps("content").get(categoryId);
		if (list==null){
			System.out.println("从数据库中查询。。。");
			TbContentExample example=new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);//指定查询为categoryId的
			criteria.andStatusEqualTo("1");//指定查询状态为“1”的
			example.setOrderByClause("sort_order");//查询的的时候根据sort_order来排序
			 list = contentMapper.selectByExample(example);
			redisTemplate.boundHashOps("content").put(categoryId,list);//存入缓存
		}else {
			System.out.println("从缓存中查询。。。");
		}
		return list;
	}

}
