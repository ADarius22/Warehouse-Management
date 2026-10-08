package com.darius.wms.stock;

public record LowStockItem(Long productId, String sku, String name, Integer minStockLevel, Long totalQuantity) {
}
