package com.darius.wms.stock;

import com.darius.wms.common.BusinessRuleException;
import com.darius.wms.common.PageResponse;
import com.darius.wms.location.LocationService;
import com.darius.wms.location.StorageLocation;
import com.darius.wms.product.Product;
import com.darius.wms.product.ProductService;
import com.darius.wms.stock.StockDtos.MovementResponse;
import com.darius.wms.stock.StockDtos.ReceiveStockRequest;
import com.darius.wms.stock.StockDtos.StockItemResponse;
import com.darius.wms.stock.StockDtos.TransferStockRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository movementRepository;
    private final ProductService productService;
    private final LocationService locationService;

    public StockService(StockItemRepository stockItemRepository, StockMovementRepository movementRepository,
                        ProductService productService, LocationService locationService) {
        this.stockItemRepository = stockItemRepository;
        this.movementRepository = movementRepository;
        this.productService = productService;
        this.locationService = locationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<StockItemResponse> search(Long productId, Long locationId, Pageable pageable) {
        return PageResponse.of(stockItemRepository.search(productId, locationId, pageable), StockItemResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovementResponse> movements(Long productId, Pageable pageable) {
        return PageResponse.of(movementRepository.search(productId, pageable), MovementResponse::from);
    }

    @Transactional(readOnly = true)
    public List<LowStockItem> lowStock() {
        return stockItemRepository.findLowStock();
    }

    /** Goods arrive at the warehouse and are put into a location. */
    public StockItemResponse receive(ReceiveStockRequest request) {
        Product product = productService.getEntity(request.productId());
        StorageLocation location = locationService.getEntity(request.locationId());
        ensureCanStore(location, request.quantity());

        StockItem item = addToLocation(product, location, request.quantity());
        movementRepository.save(StockMovement.inbound(product, location, request.quantity(), request.reference()));
        return StockItemResponse.from(item);
    }

    /** Move goods from one location to another (e.g. from bulk storage to a picking location). */
    public StockItemResponse transfer(TransferStockRequest request) {
        if (request.fromLocationId().equals(request.toLocationId())) {
            throw new BusinessRuleException("Source and destination locations must be different");
        }
        Product product = productService.getEntity(request.productId());
        StorageLocation from = locationService.getEntity(request.fromLocationId());
        StorageLocation to = locationService.getEntity(request.toLocationId());
        ensureCanStore(to, request.quantity());

        StockItem source = stockItemRepository.findByProductIdAndLocationId(product.getId(), from.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "There is no " + product.getSku() + " in location " + from.getCode()));
        removeFrom(source, request.quantity());

        StockItem destination = addToLocation(product, to, request.quantity());
        movementRepository.save(
                StockMovement.transfer(product, from, to, request.quantity(), request.reference()));
        return StockItemResponse.from(destination);
    }

    /**
     * Takes {@code quantity} units of a product out of the warehouse, spreading the pick over
     * as many locations as needed. Called by order picking inside its transaction, so if any
     * order line cannot be fulfilled the whole pick is rolled back.
     */
    public void pick(Product product, int quantity, String reference) {
        List<StockItem> available = stockItemRepository.findPickableForUpdate(product.getId());
        long total = available.stream().mapToLong(StockItem::getQuantity).sum();
        if (total < quantity) {
            throw new BusinessRuleException("Not enough stock for " + product.getSku()
                    + ": requested " + quantity + ", available " + total);
        }
        int remaining = quantity;
        for (StockItem item : available) {
            if (remaining == 0) {
                break;
            }
            int taken = Math.min(remaining, item.getQuantity());
            removeFrom(item, taken);
            movementRepository.save(StockMovement.outbound(product, item.getLocation(), taken, reference));
            remaining -= taken;
        }
    }

    private StockItem addToLocation(Product product, StorageLocation location, int quantity) {
        StockItem item = stockItemRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> new StockItem(product, location));
        item.add(quantity);
        return stockItemRepository.save(item);
    }

    private void removeFrom(StockItem item, int quantity) {
        item.remove(quantity);
        if (item.getQuantity() == 0) {
            stockItemRepository.delete(item);
        }
    }

    private void ensureCanStore(StorageLocation location, int quantity) {
        if (!location.isActive()) {
            throw new BusinessRuleException("Location " + location.getCode() + " is inactive");
        }
        long used = stockItemRepository.sumQuantityByLocation(location.getId());
        if (used + quantity > location.getCapacity()) {
            throw new BusinessRuleException("Location " + location.getCode() + " has room for only "
                    + (location.getCapacity() - used) + " more units");
        }
    }
}
