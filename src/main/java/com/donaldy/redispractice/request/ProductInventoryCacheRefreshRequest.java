package com.donaldy.redispractice.request;

import com.donaldy.redispractice.model.ProductInventory;
import com.donaldy.redispractice.service.ProductInventoryService;

/**
 * 重新加载商品库存的缓存请求
 */
public class ProductInventoryCacheRefreshRequest implements CacheRequest{

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 商品库存服务
     */
    private ProductInventoryService productInventoryService;

    public ProductInventoryCacheRefreshRequest(Integer productId, ProductInventoryService productInventoryService) {
        this.productId = productId;
        this.productInventoryService = productInventoryService;
    }

    /**
     * 处理请求
     *
     * 1. 从数据库中查询最新的商品库存数量
     * 2. 将最新的商品库存数量，刷新到Redis缓存中
     */
    @Override
    public void process() {

        ProductInventory productInventory = this.productInventoryService.getProductInventoryById(this.productId);

        this.productInventoryService.setProductInventoryCache(productInventory);
    }

    @Override
    public Integer getProductId() {

        return this.productId;
    }
}
