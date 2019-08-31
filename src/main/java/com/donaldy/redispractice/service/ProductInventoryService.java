package com.donaldy.redispractice.service;

import com.donaldy.redispractice.model.ProductInventory;

public interface ProductInventoryService {

    /**
     * 更新商品库存
     *
     * @param productInventory 商品库存
     */
    void updateProductInventory(ProductInventory productInventory);

    /**
     * 删除Redis商品库存
     * @param productInventory 商品库存
     */
    void removeProductInventoryCache(ProductInventory productInventory);

    /**
     * 获取商品库存信息
     *
     * @param productId 商品Id
     * @return          商品
     */
    ProductInventory getProductInventoryById(Integer productId);

    /**
     * 设置缓存商品库存
     *
     * @param productInventory 商品库存
     */
    void setProductInventoryCache(ProductInventory productInventory);
}
