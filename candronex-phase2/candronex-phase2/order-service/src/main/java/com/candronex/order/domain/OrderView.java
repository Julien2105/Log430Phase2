package com.candronex.order.domain;

import java.util.Map;

/** Commande + états de ses services (absence d'état = PENDING). */
public record OrderView(Order order, Map<LineId, ServiceState> serviceStates) {

    public ServiceState stateOf(LineId id) { return serviceStates.getOrDefault(id, ServiceState.PENDING); }

    public OrderState state() {
        return OrderStateCalculator.compute(order.lines().stream().map(l -> stateOf(l.id())).toList());
    }
}
