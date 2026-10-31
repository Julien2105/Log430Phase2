package com.candronex.order.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByIdAndClientId(UUID id, String clientId);

    List<OrderJpaEntity> findByClientIdOrderByAcceptedAtDesc(String clientId);
}
