package com.candronex.order.domain;

import java.util.List;

/** Dérive l'état global d'une commande à partir des états de ses services (lus auprès de SliceService). */
public final class OrderStateCalculator {

    private OrderStateCalculator() { }

    public static OrderState compute(List<ServiceState> states) {
        if (states.isEmpty() || states.stream().allMatch(s -> s == ServiceState.PENDING)) return OrderState.ACCEPTED;
        if (states.stream().allMatch(s -> s == ServiceState.ACTIVE)) return OrderState.ACTIVE;
        if (states.stream().allMatch(s -> s == ServiceState.FAILED)) return OrderState.FAILED;
        boolean inFlight = states.stream().anyMatch(s -> s == ServiceState.PENDING || s == ServiceState.ACTIVATING);
        return inFlight ? OrderState.IN_PROGRESS : OrderState.PARTIALLY_ACTIVE;
    }
}
