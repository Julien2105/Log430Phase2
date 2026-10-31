package com.candronex.inventory.api;

import com.candronex.inventory.domain.ServiceInstance;
import java.time.Instant;

/** Ne contient jamais l'IMSI ni les détails de provisionnement réseau. */
public record ServiceInstanceResponse(String serviceId, String orderId, String droneId,
                                      String serviceOfferingId, String state, Instant activatedAt) {

    public static ServiceInstanceResponse from(ServiceInstance s) {
        return new ServiceInstanceResponse(s.serviceId().toString(), s.orderId().toString(), s.droneId(),
                s.serviceOfferingId(), s.state(), s.activatedAt());
    }
}
