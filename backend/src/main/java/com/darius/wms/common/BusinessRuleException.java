package com.darius.wms.common;

/**
 * Thrown when a request is valid but violates a warehouse rule
 * (not enough stock, location full, invalid order status change...). Mapped to HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
