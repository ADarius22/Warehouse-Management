package com.darius.wms.order;

import com.darius.wms.common.PageResponse;
import com.darius.wms.order.OrderDtos.CreateOrderRequest;
import com.darius.wms.order.OrderDtos.OrderResponse;
import com.darius.wms.order.OrderDtos.OrderSummary;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageResponse<OrderSummary> list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/{id}/pick")
    public OrderResponse pick(@PathVariable Long id) {
        return orderService.pick(id);
    }

    @PostMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable Long id) {
        return orderService.ship(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }
}
