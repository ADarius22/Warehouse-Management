package com.darius.wms.stock;

import com.darius.wms.common.PageResponse;
import com.darius.wms.stock.StockDtos.MovementResponse;
import com.darius.wms.stock.StockDtos.ReceiveStockRequest;
import com.darius.wms.stock.StockDtos.StockItemResponse;
import com.darius.wms.stock.StockDtos.TransferStockRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public PageResponse<StockItemResponse> search(@RequestParam(required = false) Long productId,
                                                  @RequestParam(required = false) Long locationId,
                                                  @PageableDefault(size = 50, sort = "location.code") Pageable pageable) {
        return stockService.search(productId, locationId, pageable);
    }

    @PostMapping("/receive")
    public StockItemResponse receive(@Valid @RequestBody ReceiveStockRequest request) {
        return stockService.receive(request);
    }

    @PostMapping("/transfer")
    public StockItemResponse transfer(@Valid @RequestBody TransferStockRequest request) {
        return stockService.transfer(request);
    }

    @GetMapping("/low-stock")
    public List<LowStockItem> lowStock() {
        return stockService.lowStock();
    }

    @GetMapping("/movements")
    public PageResponse<MovementResponse> movements(
            @RequestParam(required = false) Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return stockService.movements(productId, pageable);
    }
}
