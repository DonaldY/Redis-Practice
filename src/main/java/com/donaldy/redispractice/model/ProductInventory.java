package com.donaldy.redispractice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInventory {

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 库存数量
     */
    private Long inventoryCnt;
}
