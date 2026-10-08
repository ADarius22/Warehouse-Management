package com.darius.wms.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/** Request and response bodies of the order API. */
public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotBlank @Size(max = 200) String customerName,
            @NotEmpty @Size(max = 100) List<@Valid OrderLineRequest> lines) {
    }

    public record OrderLineRequest(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(1_000_000) Integer quantity) {
    }

    /** Compact representation for lists. */
    public record OrderSummary(
            Long id,
            String orderNumber,
            String customerName,
            OrderStatus status,
            int lineCount,
            long totalUnits,
            Instant createdAt) {

        public static OrderSummary from(CustomerOrder o) {
            return new OrderSummary(o.getId(), o.getOrderNumber(), o.getCustomerName(), o.getStatus(),
                    o.getLines().size(), o.getLines().stream().mapToLong(OrderLine::getQuantity).sum(),
                    o.getCreatedAt());
        }
    }

    public record OrderLineResponse(Long id, Long productId, String sku, String productName, int quantity) {

        public static OrderLineResponse from(OrderLine l) {
            return new OrderLineResponse(l.getId(), l.getProduct().getId(), l.getProduct().getSku(),
                    l.getProduct().getName(), l.getQuantity());
        }
    }

    public record OrderResponse(
            Long id,
            String orderNumber,
            String customerName,
            OrderStatus status,
            List<OrderLineResponse> lines,
            Instant createdAt,
            Instant updatedAt,
            Instant pickedAt,
            Instant shippedAt) {

        public static OrderResponse from(CustomerOrder o) {
            return new OrderResponse(o.getId(), o.getOrderNumber(), o.getCustomerName(), o.getStatus(),
                    o.getLines().stream().map(OrderLineResponse::from).toList(),
                    o.getCreatedAt(), o.getUpdatedAt(), o.getPickedAt(), o.getShippedAt());
        }
    }
}
