package com.donaldy.redispractice.dao;

import com.donaldy.redispractice.model.ProductInventory;
import org.apache.ibatis.annotations.Param;

/**
 * 库存数量
 */
public interface ProductInventoryDao {

    /**
     * 更新库存数量
     *
     * @param productId    商品ID
     * @param inventoryCnt 库存数量
     */
    int updateInventoryByProductId(@Param("productId") Integer productId, @Param("inventoryCnt") Long inventoryCnt);

    /**
     * 根据商品ID，获取商品库存
     *
     * @param productId 商品ID
     * @return          商品库存
     */
    ProductInventory selectInventoryByProductId(@Param("productId") Integer productId);
}
