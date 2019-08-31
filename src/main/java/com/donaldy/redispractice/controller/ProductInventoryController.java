package com.donaldy.redispractice.controller;

import com.donaldy.redispractice.common.ServerResponse;
import com.donaldy.redispractice.model.ProductInventory;
import com.donaldy.redispractice.request.CacheRequest;
import com.donaldy.redispractice.request.ProductInventoryCacheRefreshRequest;
import com.donaldy.redispractice.request.ProductInventoryDBUpdateRequest;
import com.donaldy.redispractice.service.ProductInventoryService;
import com.donaldy.redispractice.service.RequestAsyncProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/inventory")
public class ProductInventoryController {

    @Autowired
    private ProductInventoryService productInventoryService;

    @Autowired
    private RequestAsyncProcessService requestAsyncProcessService;

    /**
     * 更新商品库存
     *
     * @param productInventory 商品库存
     * @return                 返回体
     */
    @GetMapping("/update")
    public ServerResponse updateProductInventory(ProductInventory productInventory) {

        CacheRequest request = new ProductInventoryDBUpdateRequest(productInventory, this.productInventoryService);

        this.requestAsyncProcessService.process(request);

        return ServerResponse.createBySuccess();
    }

    /**
     * 获取商品库存
     *
     * @param productId 商品ID
     * @return          返回体
     */
    @GetMapping("/")
    public ServerResponse updateProductInventory(Integer productId) {

        CacheRequest request = new ProductInventoryCacheRefreshRequest(productId, this.productInventoryService);

        this.requestAsyncProcessService.process(request);

        // 将请求扔给 service 异步去处理，就需要 while(true)一会儿
        // 去尝试等待前面有商品库存更新的操作，同时缓存刷新的操作，将最新的数据刷新到缓存中

        long startTime = System.currentTimeMillis();
        long endTime = 0L;
        long waitTime = 0L;

        while (waitTime <= 200) {

            // 尝试去 redis 中读取一次商品库存的缓存数据
            ProductInventory productInventory = this.productInventoryService.getProductInventoryWithCache(productId);

            // 如果读取到了结果，那么就返回
            if (productInventory != null) {

                return ServerResponse.createBySuccess(productInventory);
            }

            try {

                Thread.sleep(20);

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            endTime = System.currentTimeMillis();
            waitTime = endTime - startTime;
        }

        return ServerResponse.createBySuccess(new ProductInventory(productId, -1L));
    }
}
