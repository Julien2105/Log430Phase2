package com.candronex.drone.domain;

import java.util.List;
import java.util.Optional;

/** Port de sortie : persistance des drones. Toute lecture est filtrée par clientId. */
public interface DroneRepository {

    Optional<Drone> find(ClientId clientId, DroneId droneId);

    List<Drone> findAll(ClientId clientId);

    boolean imsiExists(Imsi imsi);

    Drone save(Drone drone);
}
