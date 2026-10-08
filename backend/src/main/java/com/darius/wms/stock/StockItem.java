package com.darius.wms.stock;

import com.darius.wms.common.BusinessRuleException;
import com.darius.wms.location.StorageLocation;
import com.darius.wms.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** Quantity of one product stored in one location. */
@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private StorageLocation location;

    @Column(nullable = false)
    private int quantity;

    /** Optimistic locking: two concurrent updates of the same row cannot silently overwrite each other. */
    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockItem() {
        // required by JPA
    }

    public StockItem(Product product, StorageLocation location) {
        this.product = product;
        this.location = location;
    }

    public void add(int amount) {
        quantity += amount;
    }

    public void remove(int amount) {
        if (amount > quantity) {
            throw new BusinessRuleException("Only " + quantity + " units of " + product.getSku()
                    + " available in " + location.getCode());
        }
        quantity -= amount;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public StorageLocation getLocation() { return location; }
    public int getQuantity() { return quantity; }
    public Instant getUpdatedAt() { return updatedAt; }
}
