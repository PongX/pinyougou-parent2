package com.pinyougou.page.service;

public interface ItemPageService {
    //根据商品id生产商品详情页
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详细页
     * @param goodsId
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
