package com.darius.wms.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        UnitOfMeasure unit,
        BigDecimal weightKg,
        int minStockLevel,
        long totalQuantity,
        boolean lowStock,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product p, long totalQuantity) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getUnit(),
                p.getWeightKg(), p.getMinStockLevel(), totalQuantity, totalQuantity < p.getMinStockLevel(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
