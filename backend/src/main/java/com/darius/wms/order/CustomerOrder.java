package com.darius.wms.order;

import com.darius.wms.common.BusinessRuleException;
import com.darius.wms.product.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Outbound order. The status transitions live here, in the entity, so the rules
 * ("only a NEW order can be picked") cannot be bypassed by any service.
 */
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    /** cascade + orphanRemoval: lines are saved/deleted together with their order. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "picked_at")
    private Instant pickedAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    protected CustomerOrder() {
        // required by JPA
    }

    public CustomerOrder(String orderNumber, String customerName) {
        this.orderNumber = orderNumber;
        this.customerName = customerName;
    }

    public void addLine(Product product, int quantity) {
        boolean duplicate = lines.stream().anyMatch(l -> l.getProduct().getId().equals(product.getId()));
        if (duplicate) {
            throw new BusinessRuleException("Product " + product.getSku() + " appears more than once in the order");
        }
        lines.add(new OrderLine(this, product, quantity));
    }

    public void markPicked() {
        requireStatus(OrderStatus.NEW, "pick");
        status = OrderStatus.PICKED;
        pickedAt = Instant.now();
    }

    public void markShipped() {
        requireStatus(OrderStatus.PICKED, "ship");
        status = OrderStatus.SHIPPED;
        shippedAt = Instant.now();
    }

    public void cancel() {
        requireStatus(OrderStatus.NEW, "cancel");
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new BusinessRuleException(
                    "Cannot " + action + " order " + orderNumber + ": status is " + status + ", expected " + expected);
        }
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerName() { return customerName; }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getPickedAt() { return pickedAt; }
    public Instant getShippedAt() { return shippedAt; }
}
