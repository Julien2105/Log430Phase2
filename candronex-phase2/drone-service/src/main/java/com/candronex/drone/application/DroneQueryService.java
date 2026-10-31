package com.candronex.drone.application;

import com.candronex.drone.domain.ClientId;
import com.candronex.drone.domain.DomainException;
import com.candronex.drone.domain.Drone;
import com.candronex.drone.domain.DroneId;
import com.candronex.drone.domain.DroneRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DroneQueryService implements DroneQuery {

    private final DroneRepository drones;

    public DroneQueryService(DroneRepository drones) { this.drones = drones; }

    @Override
    public Drone get(ClientId clientId, DroneId droneId) {
        // Drone inexistant OU appartenant à un autre client : même réponse 404 (aucune divulgation).
        return drones.find(clientId, droneId).orElseThrow(() -> DomainException.notFound("Drone introuvable"));
    }

    @Override
    public List<Drone> list(ClientId clientId) { return drones.findAll(clientId); }
}
