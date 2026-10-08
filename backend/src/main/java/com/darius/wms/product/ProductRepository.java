package com.darius.wms.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    @Query("""
            select p from Product p
            where lower(p.sku) like lower(concat('%', :term, '%'))
               or lower(p.name) like lower(concat('%', :term, '%'))
            """)
    Page<Product> search(@Param("term") String term, Pageable pageable);
}
