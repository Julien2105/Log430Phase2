package com.candronex.order.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, IdempotencyKeyId> { }
