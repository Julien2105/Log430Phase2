package com.candronex.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.candronex.order.domain.OrderState;
import com.candronex.order.domain.OrderStateCalculator;
import com.candronex.order.domain.ServiceState;
import java.util.List;
import org.junit.jupiter.api.Test;

/** États de commande : ACCEPTED, IN_PROGRESS, ACTIVE, PARTIALLY_ACTIVE, FAILED. */
class OrderStateCalculatorTest {

    @Test void noStateYetMeansAccepted() { assertThat(OrderStateCalculator.compute(List.of())).isEqualTo(OrderState.ACCEPTED); }

    @Test void allPendingIsAccepted() {
        assertThat(OrderStateCalculator.compute(List.of(ServiceState.PENDING, ServiceState.PENDING))).isEqualTo(OrderState.ACCEPTED);
    }

    @Test void activatingIsInProgress() {
        assertThat(OrderStateCalculator.compute(List.of(ServiceState.ACTIVE, ServiceState.ACTIVATING))).isEqualTo(OrderState.IN_PROGRESS);
    }

    @Test void allActiveIsActive() {
        assertThat(OrderStateCalculator.compute(List.of(ServiceState.ACTIVE, ServiceState.ACTIVE))).isEqualTo(OrderState.ACTIVE);
    }

    @Test void allFailedIsFailed() {
        assertThat(OrderStateCalculator.compute(List.of(ServiceState.FAILED, ServiceState.FAILED))).isEqualTo(OrderState.FAILED);
    }

    @Test void mixOfActiveAndFailedIsPartiallyActive() {
        assertThat(OrderStateCalculator.compute(List.of(ServiceState.ACTIVE, ServiceState.FAILED))).isEqualTo(OrderState.PARTIALLY_ACTIVE);
    }
}
