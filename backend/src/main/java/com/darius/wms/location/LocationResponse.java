package com.darius.wms.location;

public record LocationResponse(
        Long id,
        String code,
        Zone zone,
        String aisle,
        int rack,
        int shelf,
        int capacity,
        long usedCapacity,
        int utilizationPercent,
        boolean active) {

    public static LocationResponse from(StorageLocation l, long used) {
        int percent = (int) Math.round(used * 100.0 / l.getCapacity());
        return new LocationResponse(l.getId(), l.getCode(), l.getZone(), l.getAisle(), l.getRack(), l.getShelf(),
                l.getCapacity(), used, percent, l.isActive());
    }
}
