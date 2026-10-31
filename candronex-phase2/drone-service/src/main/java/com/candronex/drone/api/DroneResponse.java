package com.candronex.drone.api;

import com.candronex.drone.domain.Drone;
import java.time.Instant;

/** Représentation publique : l'IMSI n'est jamais exposé. */
public record DroneResponse(String droneId, String label, Instant registeredAt) {

    public static DroneResponse from(Drone d) {
        return new DroneResponse(d.id().value(), d.label(), d.registeredAt());
    }
}
