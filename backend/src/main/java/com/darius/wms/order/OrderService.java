package com.darius.wms.order;

import com.darius.wms.common.NotFoundException;
import com.darius.wms.common.PageResponse;
import com.darius.wms.order.OrderDtos.CreateOrderRequest;
import com.darius.wms.order.OrderDtos.OrderLineRequest;
import com.darius.wms.order.OrderDtos.OrderResponse;
import com.darius.wms.order.OrderDtos.OrderSummary;
import com.darius.wms.product.ProductService;
import com.darius.wms.stock.StockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final StockService stockService;

    public OrderService(OrderRepository orderRepository, ProductService productService, StockService stockService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummary> list(OrderStatus status, Pageable pageable) {
        Page<CustomerOrder> page = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        // Lines are loaded lazily in batches (hibernate.default_batch_fetch_size), not one query per order
        return PageResponse.of(page, OrderSummary::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(getWithLines(id));
    }

    public OrderResponse create(CreateOrderRequest request) {
        String number = "ORD-%06d".formatted(orderRepository.nextOrderNumber());
        CustomerOrder order = new CustomerOrder(number, request.customerName().trim());
        for (OrderLineRequest line : request.lines()) {
            order.addLine(productService.getEntity(line.productId()), line.quantity());
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    /**
     * Picks every line of the order from stock. Runs in ONE transaction: if a single line
     * cannot be fulfilled, nothing is taken out of stock and the order stays NEW.
     */
    public OrderResponse pick(Long id) {
        CustomerOrder order = getWithLines(id);
        order.markPicked();
        for (OrderLine line : order.getLines()) {
            stockService.pick(line.getProduct(), line.getQuantity(), order.getOrderNumber());
        }
        return OrderResponse.from(order);
    }

    public OrderResponse ship(Long id) {
        CustomerOrder order = getWithLines(id);
        order.markShipped();
        return OrderResponse.from(order);
    }

    public OrderResponse cancel(Long id) {
        CustomerOrder order = getWithLines(id);
        order.cancel();
        return OrderResponse.from(order);
    }

    private CustomerOrder getWithLines(Long id) {
        return orderRepository.findWithLinesById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }
}
