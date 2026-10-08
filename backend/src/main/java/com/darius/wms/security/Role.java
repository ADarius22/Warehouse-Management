package com.darius.wms.security;

public enum Role {
    /** Full access, including product and location master data. */
    ADMIN,
    /** Warehouse floor: receive and move stock, create, pick and ship orders. */
    OPERATOR
}
