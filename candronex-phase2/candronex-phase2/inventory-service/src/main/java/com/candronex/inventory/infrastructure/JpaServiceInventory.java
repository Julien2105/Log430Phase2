package com.candronex.inventory.infrastructure;

import com.candronex.inventory.domain.ClientId;
import com.candronex.inventory.domain.DomainException;
import com.candronex.inventory.domain.ServiceInstance;
import com.candronex.inventory.domain.ServiceInventoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur JPA du port ServiceInventoryRepository (base inventory_db). */
@Repository
public class JpaServiceInventory implements ServiceInventoryRepository {

    private final ServiceInstanceJpaRepository jpa;

    public JpaServiceInventory(ServiceInstanceJpaRepository jpa) { this.jpa = jpa; }

    @Override
    @Transactional
    public ServiceInstance upsert(ServiceInstance s) {
        // Un service appartient à un seul client : refuser l'écrasement par un autre client.
        jpa.findById(s.serviceId()).ifPresent(existing -> {
            if (!existing.getClientId().equals(s.clientId().value())) {
                throw DomainException.conflict("SERVICE_CONFLICT", "Ce service appartient à un autre client");
            }
        });
        jpa.saveAndFlush(new ServiceInstanceJpaEntity(s.serviceId(), s.clientId().value(), s.orderId(), s.droneId(),
                s.serviceOfferingId(), s.state(), s.activatedAt()));
        return s;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceInstance> find(ClientId clientId, UUID serviceId) {
        return jpa.findByServiceIdAndClientId(serviceId, clientId.value()).map(JpaServiceInventory::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> findAll(ClientId clientId) {
        return jpa.findByClientIdOrderByActivatedAt(clientId.value()).stream().map(JpaServiceInventory::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> findByDrone(ClientId clientId, String droneId) {
        return jpa.findByClientIdAndDroneIdOrderByActivatedAt(clientId.value(), droneId).stream()
                .map(JpaServiceInventory::toDomain).toList();
    }

    private static ServiceInstance toDomain(ServiceInstanceJpaEntity e) {
        return new ServiceInstance(e.getServiceId(), ClientId.of(e.getClientId()), e.getOrderId(), e.getDroneId(),
                e.getServiceOfferingId(), e.getState(), e.getActivatedAt());
    }
}
