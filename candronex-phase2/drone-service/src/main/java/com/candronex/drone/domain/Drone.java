package com.candronex.drone.domain;

import java.time.Instant;
import java.util.Objects;

/** Agrégat Drone : identifié par (clientId, droneId). */
public record Drone(ClientId clientId, DroneId id, Imsi imsi, String label, Instant registeredAt) {

    public boolean sameContentAs(Imsi otherImsi, String otherLabel) {
        return imsi.equals(otherImsi) && Objects.equals(label, otherLabel);
    }
}
