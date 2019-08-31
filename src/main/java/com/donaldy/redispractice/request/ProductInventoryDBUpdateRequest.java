package com.donaldy.redispractice.request;

import com.donaldy.redispractice.model.ProductInventory;
import com.donaldy.redispractice.service.ProductInventoryService;

/**
 * 商品发生了交易，那么就要修改这个商品对应的库存
 *
 * 此时就会发送请求过来，要求修改库存
 *
 * 数据更新请求
 *
 * cache aside pattern
 *
 * (1) 删除缓存
 * (2) 更新数据库
 */
public class ProductInventoryDBUpdateRequest implements CacheRequest {

    /**
     * 商品库存
     */
    private ProductInventory productInventory;

    /**
     * 商品库存服务
     */
    private ProductInventoryService productInventoryService;

    public ProductInventoryDBUpdateRequest(ProductInventory productInventory, ProductInventoryService productInventoryService) {
        this.productInventory = productInventory;
        this.productInventoryService = productInventoryService;
    }

    public void process() {
        // 删除 redis 中的缓存
        this.productInventoryService.removeProductInventoryCache(productInventory);
        // 修改数据库中的库存
        this.productInventoryService.updateProductInventory(productInventory);
    }

    @Override
    public Integer getProductId() {

        return this.productInventory.getProductId();
    }
}
