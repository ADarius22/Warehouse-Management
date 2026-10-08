package com.darius.wms.stock;

public enum MovementType {
    /** Goods arriving into the warehouse. */
    INBOUND,
    /** Goods moved between two locations. */
    TRANSFER,
    /** Goods leaving the warehouse (picked for an order). */
    OUTBOUND
}
