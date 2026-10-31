package com.candronex.order.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id @Column(name = "id") private UUID id;
    @Column(name = "client_id") private String clientId;
    @Column(name = "accepted_at") private Instant acceptedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderLineJpaEntity> lines = new ArrayList<>();

    protected OrderJpaEntity() { }

    public OrderJpaEntity(UUID id, String clientId, Instant acceptedAt) {
        this.id = id;
        this.clientId = clientId;
        this.acceptedAt = acceptedAt;
    }

    public UUID getId() { return id; }
    public String getClientId() { return clientId; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public List<OrderLineJpaEntity> getLines() { return lines; }
}
