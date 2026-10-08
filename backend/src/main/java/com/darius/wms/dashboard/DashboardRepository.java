package com.darius.wms.dashboard;

import com.darius.wms.location.StorageLocation;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/** Read-only reporting queries written in plain SQL. */
public interface DashboardRepository extends Repository<StorageLocation, Long> {

    /** Capacity vs. stored units per warehouse zone (active locations only). */
    @Query(nativeQuery = true, value = """
            SELECT l.zone                       AS "zone",
                   COUNT(*)                     AS "locationCount",
                   SUM(l.capacity)              AS "totalCapacity",
                   COALESCE(SUM(s.used), 0)     AS "usedCapacity"
            FROM storage_locations l
            LEFT JOIN (SELECT location_id, SUM(quantity) AS used
                       FROM stock_items
                       GROUP BY location_id) s ON s.location_id = l.id
            WHERE l.active = TRUE
            GROUP BY l.zone
            ORDER BY l.zone
            """)
    List<ZoneUtilizationRow> zoneUtilization();

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM products")
    long countProducts();

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM storage_locations WHERE active = TRUE")
    long countActiveLocations();

    /** Spring Data fills this interface from the column aliases of the native query. */
    interface ZoneUtilizationRow {
        String getZone();
        Number getLocationCount();
        Number getTotalCapacity();
        Number getUsedCapacity();
    }
}
