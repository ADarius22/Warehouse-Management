package com.darius.wms.stock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @EntityGraph(attributePaths = {"product", "fromLocation", "toLocation"})
    @Query("select m from StockMovement m where (:productId is null or m.product.id = :productId)")
    Page<StockMovement> search(@Param("productId") Long productId, Pageable pageable);
}
