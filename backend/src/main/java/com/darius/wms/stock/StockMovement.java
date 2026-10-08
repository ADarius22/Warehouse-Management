package com.darius.wms.stock;

import com.darius.wms.location.StorageLocation;
import com.darius.wms.product.Product;
import com.darius.wms.security.CurrentUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Immutable audit record of a stock change. Rows are only ever inserted, never updated. */
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private StorageLocation fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private StorageLocation toLocation;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(length = 100)
    private String reference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Username of the logged-in user who caused the movement. */
    @Column(name = "performed_by", length = 50, updatable = false)
    private String performedBy;

    protected StockMovement() {
        // required by JPA
    }

    private StockMovement(MovementType type, Product product, StorageLocation from, StorageLocation to,
                          int quantity, String reference) {
        this.type = type;
        this.product = product;
        this.fromLocation = from;
        this.toLocation = to;
        this.quantity = quantity;
        this.reference = reference;
        this.createdAt = Instant.now();
        this.performedBy = CurrentUser.username();
    }

    public static StockMovement inbound(Product product, StorageLocation to, int quantity, String reference) {
        return new StockMovement(MovementType.INBOUND, product, null, to, quantity, reference);
    }

    public static StockMovement transfer(Product product, StorageLocation from, StorageLocation to,
                                         int quantity, String reference) {
        return new StockMovement(MovementType.TRANSFER, product, from, to, quantity, reference);
    }

    public static StockMovement outbound(Product product, StorageLocation from, int quantity, String reference) {
        return new StockMovement(MovementType.OUTBOUND, product, from, null, quantity, reference);
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public StorageLocation getFromLocation() { return fromLocation; }
    public StorageLocation getToLocation() { return toLocation; }
    public int getQuantity() { return quantity; }
    public MovementType getType() { return type; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
    public String getPerformedBy() { return performedBy; }
}
