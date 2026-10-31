package com.candronex.inventory.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInstanceJpaRepository extends JpaRepository<ServiceInstanceJpaEntity, UUID> {

    Optional<ServiceInstanceJpaEntity> findByServiceIdAndClientId(UUID serviceId, String clientId);

    List<ServiceInstanceJpaEntity> findByClientIdOrderByActivatedAt(String clientId);

    List<ServiceInstanceJpaEntity> findByClientIdAndDroneIdOrderByActivatedAt(String clientId, String droneId);
}
