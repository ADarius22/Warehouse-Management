package com.darius.wms.stock;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByProductIdAndLocationId(Long productId, Long locationId);

    @EntityGraph(attributePaths = {"product", "location"})
    @Query("""
            select s from StockItem s
            where (:productId is null or s.product.id = :productId)
              and (:locationId is null or s.location.id = :locationId)
            """)
    Page<StockItem> search(@Param("productId") Long productId,
                           @Param("locationId") Long locationId,
                           Pageable pageable);

    /**
     * Stock rows of a product that can be picked, biggest first.
     * PESSIMISTIC_WRITE = SELECT ... FOR UPDATE: two orders picking the same product
     * at the same time are serialized, so stock can never be double-allocated.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from StockItem s
            where s.product.id = :productId and s.quantity > 0 and s.location.active = true
            order by s.quantity desc, s.id
            """)
    List<StockItem> findPickableForUpdate(@Param("productId") Long productId);

    @Query("select coalesce(sum(s.quantity), 0L) from StockItem s where s.product.id = :productId")
    long sumQuantityByProduct(@Param("productId") Long productId);

    @Query("select coalesce(sum(s.quantity), 0L) from StockItem s where s.location.id = :locationId")
    long sumQuantityByLocation(@Param("locationId") Long locationId);

    @Query("select coalesce(sum(s.quantity), 0L) from StockItem s")
    long sumAllQuantities();

    @Query("""
            select s.product.id, sum(s.quantity) from StockItem s
            where s.product.id in :productIds group by s.product.id
            """)
    List<Object[]> sumGroupedByProduct(@Param("productIds") Collection<Long> productIds);

    @Query("""
            select s.location.id, sum(s.quantity) from StockItem s
            where s.location.id in :locationIds group by s.location.id
            """)
    List<Object[]> sumGroupedByLocation(@Param("locationIds") Collection<Long> locationIds);

    /** Products whose total stock across all locations is below their minimum level. */
    @Query("""
            select new com.darius.wms.stock.LowStockItem(
                p.id, p.sku, p.name, p.minStockLevel, coalesce(sum(s.quantity), 0L))
            from Product p left join StockItem s on s.product = p
            group by p.id, p.sku, p.name, p.minStockLevel
            having coalesce(sum(s.quantity), 0L) < p.minStockLevel
            order by p.sku
            """)
    List<LowStockItem> findLowStock();

    default Map<Long, Long> totalsByProduct(Collection<Long> productIds) {
        return productIds.isEmpty() ? Map.of() : toMap(sumGroupedByProduct(productIds));
    }

    default Map<Long, Long> totalsByLocation(Collection<Long> locationIds) {
        return locationIds.isEmpty() ? Map.of() : toMap(sumGroupedByLocation(locationIds));
    }

    private static Map<Long, Long> toMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()));
    }
}
