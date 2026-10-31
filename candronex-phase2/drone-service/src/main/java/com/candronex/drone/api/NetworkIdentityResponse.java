package com.candronex.drone.api;

import com.candronex.drone.domain.Drone;

/** Contrat interne drone-service -> order-service (route non publiée par la passerelle). */
public record NetworkIdentityResponse(String droneId, String imsi) {

    public static NetworkIdentityResponse from(Drone d) {
        return new NetworkIdentityResponse(d.id().value(), d.imsi().value());
    }
}
