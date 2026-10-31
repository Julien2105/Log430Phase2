package com.candronex.drone.support;

import com.candronex.drone.domain.ClientId;
import com.candronex.drone.domain.Drone;
import com.candronex.drone.domain.DroneId;
import com.candronex.drone.domain.DroneRepository;
import com.candronex.drone.domain.Imsi;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryDroneRepository implements DroneRepository {

    private final List<Drone> store = new ArrayList<>();

    @Override
    public Optional<Drone> find(ClientId c, DroneId d) {
        return store.stream().filter(x -> x.clientId().equals(c) && x.id().equals(d)).findFirst();
    }

    @Override
    public List<Drone> findAll(ClientId c) { return store.stream().filter(x -> x.clientId().equals(c)).toList(); }

    @Override
    public boolean imsiExists(Imsi imsi) { return store.stream().anyMatch(x -> x.imsi().equals(imsi)); }

    @Override
    public Drone save(Drone d) { store.add(d); return d; }

    public int size() { return store.size(); }
}
