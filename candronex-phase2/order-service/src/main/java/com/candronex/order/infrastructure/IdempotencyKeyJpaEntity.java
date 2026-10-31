package com.candronex.order.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_key")
@IdClass(IdempotencyKeyId.class)
public class IdempotencyKeyJpaEntity {

    @Id @Column(name = "client_id") private String clientId;
    @Id @Column(name = "idempotency_key") private String idempotencyKey;
    @Column(name = "request_hash") private String requestHash;
    @Column(name = "order_id") private UUID orderId;
    @Column(name = "created_at") private Instant createdAt;

    protected IdempotencyKeyJpaEntity() { }

    public IdempotencyKeyJpaEntity(String clientId, String idempotencyKey, String requestHash, UUID orderId, Instant createdAt) {
        this.clientId = clientId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    public String getRequestHash() { return requestHash; }
    public UUID getOrderId() { return orderId; }
}
