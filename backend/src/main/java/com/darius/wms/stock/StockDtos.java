package com.darius.wms.stock;

import com.darius.wms.location.Zone;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Request and response bodies of the stock API. */
public final class StockDtos {

    private StockDtos() {
    }

    public record ReceiveStockRequest(
            @NotNull Long productId,
            @NotNull Long locationId,
            @NotNull @Min(1) @Max(1_000_000) Integer quantity,
            @Size(max = 100) String reference) {
    }

    public record TransferStockRequest(
            @NotNull Long productId,
            @NotNull Long fromLocationId,
            @NotNull Long toLocationId,
            @NotNull @Min(1) @Max(1_000_000) Integer quantity,
            @Size(max = 100) String reference) {
    }

    public record StockItemResponse(
            Long id,
            Long productId,
            String sku,
            String productName,
            Long locationId,
            String locationCode,
            Zone zone,
            int quantity,
            Instant updatedAt) {

        public static StockItemResponse from(StockItem s) {
            return new StockItemResponse(s.getId(), s.getProduct().getId(), s.getProduct().getSku(),
                    s.getProduct().getName(), s.getLocation().getId(), s.getLocation().getCode(),
                    s.getLocation().getZone(), s.getQuantity(), s.getUpdatedAt());
        }
    }

    public record MovementResponse(
            Long id,
            MovementType type,
            Long productId,
            String sku,
            String productName,
            String fromLocation,
            String toLocation,
            int quantity,
            String reference,
            String performedBy,
            Instant createdAt) {

        public static MovementResponse from(StockMovement m) {
            return new MovementResponse(m.getId(), m.getType(), m.getProduct().getId(), m.getProduct().getSku(),
                    m.getProduct().getName(),
                    m.getFromLocation() == null ? null : m.getFromLocation().getCode(),
                    m.getToLocation() == null ? null : m.getToLocation().getCode(),
                    m.getQuantity(), m.getReference(), m.getPerformedBy(), m.getCreatedAt());
        }
    }
}
