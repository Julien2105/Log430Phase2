package com.candronex.slice.api;

import com.candronex.slice.domain.Service;
import java.time.Instant;

/** Ne contient jamais l'IMSI. */
public record ServiceStatusResponse(String serviceId, String orderId, String droneId, String serviceOfferingId,
                                    String state, Instant acceptedAt, Instant activatedAt, String failureReason) {

    public static ServiceStatusResponse from(Service s) {
        return new ServiceStatusResponse(s.serviceId().toString(), s.orderId().toString(), s.droneId(),
                s.serviceOfferingId(), s.state().name(), s.acceptedAt(), s.activatedAt(), s.failureReason());
    }
}
