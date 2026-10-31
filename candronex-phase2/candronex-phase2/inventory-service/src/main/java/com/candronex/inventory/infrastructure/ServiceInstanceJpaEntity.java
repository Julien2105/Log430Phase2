package com.candronex.inventory.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_instance")
public class ServiceInstanceJpaEntity {

    @Id @Column(name = "service_id") private UUID serviceId;
    @Column(name = "client_id") private String clientId;
    @Column(name = "order_id") private UUID orderId;
    @Column(name = "drone_id") private String droneId;
    @Column(name = "service_offering_id") private String serviceOfferingId;
    @Column(name = "state") private String state;
    @Column(name = "activated_at") private Instant activatedAt;

    protected ServiceInstanceJpaEntity() { }

    public ServiceInstanceJpaEntity(UUID serviceId, String clientId, UUID orderId, String droneId,
                                    String serviceOfferingId, String state, Instant activatedAt) {
        this.serviceId = serviceId;
        this.clientId = clientId;
        this.orderId = orderId;
        this.droneId = droneId;
        this.serviceOfferingId = serviceOfferingId;
        this.state = state;
        this.activatedAt = activatedAt;
    }

    public UUID getServiceId() { return serviceId; }
    public String getClientId() { return clientId; }
    public UUID getOrderId() { return orderId; }
    public String getDroneId() { return droneId; }
    public String getServiceOfferingId() { return serviceOfferingId; }
    public String getState() { return state; }
    public Instant getActivatedAt() { return activatedAt; }
}
