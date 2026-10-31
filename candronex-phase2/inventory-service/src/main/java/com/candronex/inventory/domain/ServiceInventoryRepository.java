package com.candronex.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie : persistance de l'inventaire (toute lecture filtrée par clientId). */
public interface ServiceInventoryRepository {

    ServiceInstance upsert(ServiceInstance instance);

    Optional<ServiceInstance> find(ClientId clientId, UUID serviceId);

    List<ServiceInstance> findAll(ClientId clientId);

    List<ServiceInstance> findByDrone(ClientId clientId, String droneId);
}
