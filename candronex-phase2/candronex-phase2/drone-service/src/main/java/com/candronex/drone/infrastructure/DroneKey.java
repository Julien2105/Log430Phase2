package com.candronex.drone.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class DroneKey implements Serializable {

    private String clientId;
    private String droneId;

    public DroneKey() { }

    public DroneKey(String clientId, String droneId) {
        this.clientId = clientId;
        this.droneId = droneId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DroneKey k && Objects.equals(clientId, k.clientId) && Objects.equals(droneId, k.droneId);
    }

    @Override
    public int hashCode() { return Objects.hash(clientId, droneId); }
}
