package com.darius.wms.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A physical slot in the warehouse, identified by aisle / rack / shelf (e.g. "A-01-03"). */
@Entity
@Table(name = "storage_locations")
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Zone zone;

    @Column(nullable = false, length = 10)
    private String aisle;

    @Column(nullable = false)
    private int rack;

    @Column(nullable = false)
    private int shelf;

    /** Maximum number of units this location can hold. */
    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean active = true;

    public static String buildCode(String aisle, int rack, int shelf) {
        return "%s-%02d-%02d".formatted(aisle.toUpperCase(), rack, shelf);
    }

    public void place(Zone zone, String aisle, int rack, int shelf) {
        this.zone = zone;
        this.aisle = aisle.toUpperCase();
        this.rack = rack;
        this.shelf = shelf;
        this.code = buildCode(aisle, rack, shelf);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public Zone getZone() { return zone; }
    public String getAisle() { return aisle; }
    public int getRack() { return rack; }
    public int getShelf() { return shelf; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
