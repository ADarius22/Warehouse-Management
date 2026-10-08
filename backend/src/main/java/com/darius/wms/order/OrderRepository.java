package com.darius.wms.order;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    /** Loads the order, its lines and their products in a single SQL query. */
    @EntityGraph(attributePaths = {"lines", "lines.product"})
    Optional<CustomerOrder> findWithLinesById(Long id);

    Page<CustomerOrder> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatusIn(Collection<OrderStatus> statuses);

    @Query(value = "select nextval('order_number_seq')", nativeQuery = true)
    long nextOrderNumber();
}
