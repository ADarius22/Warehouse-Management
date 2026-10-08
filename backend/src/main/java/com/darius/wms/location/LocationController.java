package com.darius.wms.location;

import com.darius.wms.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public PageResponse<LocationResponse> list(@RequestParam(required = false) Zone zone,
                                               @RequestParam(required = false) String search,
                                               @PageableDefault(size = 50, sort = "code") Pageable pageable) {
        return locationService.list(zone, search, pageable);
    }

    @GetMapping("/{id}")
    public LocationResponse get(@PathVariable Long id) {
        return locationService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return locationService.create(request);
    }

    @PutMapping("/{id}")
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
