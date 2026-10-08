package com.darius.wms.order;

/**
 * Lifecycle of an outbound order:
 * NEW -> PICKED -> SHIPPED, or NEW -> CANCELLED.
 */
public enum OrderStatus {
    NEW, PICKED, SHIPPED, CANCELLED
}
