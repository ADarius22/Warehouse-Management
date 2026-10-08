package com.darius.wms.location;

import com.darius.wms.common.BusinessRuleException;
import com.darius.wms.common.NotFoundException;
import com.darius.wms.common.PageResponse;
import com.darius.wms.stock.StockItemRepository;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final StockItemRepository stockItemRepository;

    public LocationService(LocationRepository locationRepository, StockItemRepository stockItemRepository) {
        this.locationRepository = locationRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<LocationResponse> list(Zone zone, String search, Pageable pageable) {
        String term = search == null ? "" : search.trim();
        Page<StorageLocation> page = zone == null
                ? locationRepository.findByCodeContainingIgnoreCase(term, pageable)
                : locationRepository.findByZoneAndCodeContainingIgnoreCase(zone, term, pageable);
        Map<Long, Long> used = stockItemRepository.totalsByLocation(page.map(StorageLocation::getId).getContent());
        return PageResponse.of(page, l -> LocationResponse.from(l, used.getOrDefault(l.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public LocationResponse get(Long id) {
        return LocationResponse.from(getEntity(id), stockItemRepository.sumQuantityByLocation(id));
    }

    public LocationResponse create(LocationRequest request) {
        String code = StorageLocation.buildCode(request.aisle(), request.rack(), request.shelf());
        if (locationRepository.existsByCode(code)) {
            throw new BusinessRuleException("Location " + code + " already exists");
        }
        StorageLocation location = new StorageLocation();
        apply(location, request);
        return LocationResponse.from(locationRepository.save(location), 0);
    }

    public LocationResponse update(Long id, LocationRequest request) {
        StorageLocation location = getEntity(id);
        String code = StorageLocation.buildCode(request.aisle(), request.rack(), request.shelf());
        if (locationRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessRuleException("Location " + code + " already exists");
        }
        long used = stockItemRepository.sumQuantityByLocation(id);
        if (request.capacity() < used) {
            throw new BusinessRuleException("Capacity cannot be lower than the " + used + " units currently stored");
        }
        apply(location, request);
        return LocationResponse.from(location, used);
    }

    public void delete(Long id) {
        StorageLocation location = getEntity(id);
        locationRepository.delete(location);
        locationRepository.flush();
    }

    @Transactional(readOnly = true)
    public StorageLocation getEntity(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new NotFoundException("Location", id));
    }

    private static void apply(StorageLocation location, LocationRequest request) {
        location.place(request.zone(), request.aisle().trim(), request.rack(), request.shelf());
        location.setCapacity(request.capacity());
        location.setActive(request.active() == null || request.active());
    }
}
