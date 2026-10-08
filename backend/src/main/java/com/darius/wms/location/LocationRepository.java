package com.darius.wms.location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<StorageLocation, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Page<StorageLocation> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<StorageLocation> findByZoneAndCodeContainingIgnoreCase(Zone zone, String code, Pageable pageable);
}
