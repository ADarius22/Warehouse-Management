package com.darius.wms.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.darius.wms.common.BusinessRuleException;
import org.junit.jupiter.api.Test;

/** Plain unit test (no Spring, no database) for the order status rules. */
class CustomerOrderTest {

    @Test
    void newOrderCanBePickedThenShipped() {
        CustomerOrder order = new CustomerOrder("ORD-1", "ACME");

        order.markPicked();
        order.markShipped();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getPickedAt()).isNotNull();
        assertThat(order.getShippedAt()).isNotNull();
    }

    @Test
    void cannotShipAnOrderThatWasNotPicked() {
        CustomerOrder order = new CustomerOrder("ORD-2", "ACME");

        assertThatThrownBy(order::markShipped)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expected PICKED");
    }

    @Test
    void cannotCancelAPickedOrder() {
        CustomerOrder order = new CustomerOrder("ORD-3", "ACME");
        order.markPicked();

        assertThatThrownBy(order::cancel).isInstanceOf(BusinessRuleException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PICKED);
    }
}
