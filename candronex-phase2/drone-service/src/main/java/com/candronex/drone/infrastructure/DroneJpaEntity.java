package com.candronex.drone.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "drone")
@IdClass(DroneKey.class)
public class DroneJpaEntity {

    @Id @Column(name = "client_id") private String clientId;
    @Id @Column(name = "drone_id") private String droneId;
    @Column(name = "imsi") private String imsi;
    @Column(name = "label") private String label;
    @Column(name = "registered_at") private Instant registeredAt;

    protected DroneJpaEntity() { }

    public DroneJpaEntity(String clientId, String droneId, String imsi, String label, Instant registeredAt) {
        this.clientId = clientId;
        this.droneId = droneId;
        this.imsi = imsi;
        this.label = label;
        this.registeredAt = registeredAt;
    }

    public String getClientId() { return clientId; }
    public String getDroneId() { return droneId; }
    public String getImsi() { return imsi; }
    public String getLabel() { return label; }
    public Instant getRegisteredAt() { return registeredAt; }
}
