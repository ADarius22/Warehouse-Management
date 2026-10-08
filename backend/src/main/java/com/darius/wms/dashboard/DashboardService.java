package com.darius.wms.dashboard;

import com.darius.wms.dashboard.DashboardResponse.ZoneUtilization;
import com.darius.wms.order.OrderRepository;
import com.darius.wms.order.OrderStatus;
import com.darius.wms.stock.StockItemRepository;
import com.darius.wms.stock.StockService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final StockItemRepository stockItemRepository;
    private final OrderRepository orderRepository;
    private final StockService stockService;

    public DashboardService(DashboardRepository dashboardRepository, StockItemRepository stockItemRepository,
                            OrderRepository orderRepository, StockService stockService) {
        this.dashboardRepository = dashboardRepository;
        this.stockItemRepository = stockItemRepository;
        this.orderRepository = orderRepository;
        this.stockService = stockService;
    }

    public DashboardResponse summary() {
        List<ZoneUtilization> zones = dashboardRepository.zoneUtilization().stream()
                .map(row -> {
                    long total = row.getTotalCapacity().longValue();
                    long used = row.getUsedCapacity().longValue();
                    int percent = total == 0 ? 0 : (int) Math.round(used * 100.0 / total);
                    return new ZoneUtilization(row.getZone(), row.getLocationCount().longValue(), total, used, percent);
                })
                .toList();

        var recent = stockService.movements(null, PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt")));

        return new DashboardResponse(
                dashboardRepository.countProducts(),
                dashboardRepository.countActiveLocations(),
                stockItemRepository.sumAllQuantities(),
                orderRepository.countByStatusIn(List.of(OrderStatus.NEW, OrderStatus.PICKED)),
                stockService.lowStock(),
                zones,
                recent.content());
    }
}
