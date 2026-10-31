package com.candronex.drone.infrastructure;

import com.candronex.drone.domain.ClientId;
import com.candronex.drone.domain.DomainException;
import com.candronex.drone.domain.Drone;
import com.candronex.drone.domain.DroneId;
import com.candronex.drone.domain.DroneRepository;
import com.candronex.drone.domain.Imsi;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur JPA du port DroneRepository (base drone_db). */
@Repository
public class JpaDroneRepository implements DroneRepository {

    private final DroneJpaRepository jpa;

    public JpaDroneRepository(DroneJpaRepository jpa) { this.jpa = jpa; }

    @Override
    @Transactional(readOnly = true)
    public Optional<Drone> find(ClientId clientId, DroneId droneId) {
        return jpa.findById(new DroneKey(clientId.value(), droneId.value())).map(JpaDroneRepository::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Drone> findAll(ClientId clientId) {
        return jpa.findByClientIdOrderByDroneId(clientId.value()).stream().map(JpaDroneRepository::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean imsiExists(Imsi imsi) { return jpa.existsByImsi(imsi.value()); }

    @Override
    @Transactional
    public Drone save(Drone d) {
        try {
            jpa.saveAndFlush(new DroneJpaEntity(d.clientId().value(), d.id().value(), d.imsi().value(), d.label(), d.registeredAt()));
            return d;
        } catch (DataIntegrityViolationException e) {
            // Course entre deux enregistrements : la contrainte d'unicité protège les données.
            throw DomainException.conflict("IMSI_ALREADY_REGISTERED", "Cet IMSI est déjà associé à un autre drone");
        }
    }

    private static Drone toDomain(DroneJpaEntity e) {
        return new Drone(ClientId.of(e.getClientId()), DroneId.of(e.getDroneId()), Imsi.of(e.getImsi()), e.getLabel(), e.getRegisteredAt());
    }
}
