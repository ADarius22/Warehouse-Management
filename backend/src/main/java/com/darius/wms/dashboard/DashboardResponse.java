package com.darius.wms.dashboard;

import com.darius.wms.stock.LowStockItem;
import com.darius.wms.stock.StockDtos.MovementResponse;
import java.util.List;

public record DashboardResponse(
        long productCount,
        long activeLocationCount,
        long totalUnits,
        long openOrders,
        List<LowStockItem> lowStock,
        List<ZoneUtilization> zones,
        List<MovementResponse> recentMovements) {

    public record ZoneUtilization(String zone, long locationCount, long totalCapacity, long usedCapacity,
                                  int utilizationPercent) {
    }
}
