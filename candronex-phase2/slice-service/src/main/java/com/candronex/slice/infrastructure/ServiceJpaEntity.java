package com.candronex.slice.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service")
public class ServiceJpaEntity {

    @Id @Column(name = "id") UUID id;
    @Column(name = "order_id") UUID orderId;
    @Column(name = "client_id") String clientId;
    @Column(name = "drone_id") String droneId;
    @Column(name = "imsi") String imsi;
    @Column(name = "service_offering_id") String serviceOfferingId;
    @Column(name = "slice_type") String sliceType;
    @Column(name = "sst") Integer sst;
    @Column(name = "sd") String sd;
    @Column(name = "dnn") String dnn;
    @Column(name = "five_qi") Integer fiveQi;
    @Column(name = "state") String state;
    @Column(name = "accepted_at") Instant acceptedAt;
    @Column(name = "activated_at") Instant activatedAt;
    @Column(name = "failure_reason") String failureReason;

    protected ServiceJpaEntity() { }
}
