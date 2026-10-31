package com.candronex.drone.application;

import com.candronex.drone.domain.ClientId;
import com.candronex.drone.domain.Drone;
import com.candronex.drone.domain.DroneId;
import java.util.List;

/** UC-02 (consultation) et identité réseau pour OrderService. */
public interface DroneQuery {

    Drone get(ClientId clientId, DroneId droneId);

    List<Drone> list(ClientId clientId);
}
