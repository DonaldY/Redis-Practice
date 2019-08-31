package com.donaldy.redispractice.service.impl;

import com.donaldy.redispractice.dao.ProductInventoryDao;
import com.donaldy.redispractice.dao.RedisDao;
import com.donaldy.redispractice.model.ProductInventory;
import com.donaldy.redispractice.service.ProductInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductInventoryServiceImpl implements ProductInventoryService {

    @Autowired
    private ProductInventoryDao productInventoryDao;

    @Autowired
    private RedisDao redisDao;

    @Override
    public void updateProductInventory(ProductInventory productInventory) {

        this.productInventoryDao.updateInventoryByProductId(productInventory.getProductId(), productInventory.getInventoryCnt());
    }

    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
        String key = "product:productInventory:" + productInventory.getProductId();
        redisDao.delete(key);
    }

    @Override
    public ProductInventory getProductInventoryById(Integer productId) {

        return this.productInventoryDao.selectInventoryByProductId(productId);
    }

    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {

        String key = "product:productInventory:" + productInventory.getProductId();

        this.redisDao.setString(key, String.valueOf(productInventory.getInventoryCnt()));
    }
}
