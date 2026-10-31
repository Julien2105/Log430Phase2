package com.candronex.slice.infrastructure;

import com.candronex.slice.domain.Characteristic;
import com.candronex.slice.domain.ClientId;
import com.candronex.slice.domain.Imsi;
import com.candronex.slice.domain.Service;
import com.candronex.slice.domain.ServiceRepository;
import com.candronex.slice.domain.ServiceState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur JPA du port ServiceRepository (base slice_db). */
@Repository
public class JpaServiceRepository implements ServiceRepository {

    private final ServiceJpaRepository jpa;

    public JpaServiceRepository(ServiceJpaRepository jpa) { this.jpa = jpa; }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> find(UUID serviceId) { return jpa.findById(serviceId).map(JpaServiceRepository::toDomain); }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findByOrder(ClientId clientId, UUID orderId) {
        return jpa.findByClientIdAndOrderIdOrderByAcceptedAt(clientId.value(), orderId).stream()
                .map(JpaServiceRepository::toDomain).toList();
    }

    @Override
    @Transactional
    public void save(Service s) { jpa.saveAndFlush(toEntity(s)); }

    @Override
    @Transactional
    public Optional<Service> claimForActivation(UUID serviceId) {
        if (jpa.claim(serviceId) != 1) return Optional.empty();
        return jpa.findById(serviceId).map(JpaServiceRepository::toDomain);
    }

    @Override
    @Transactional
    public int resetInterruptedActivations() { return jpa.resetActivating(); }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPendingIds() { return jpa.findPendingIds(); }

    private static ServiceJpaEntity toEntity(Service s) {
        ServiceJpaEntity e = new ServiceJpaEntity();
        e.id = s.serviceId();
        e.orderId = s.orderId();
        e.clientId = s.clientId().value();
        e.droneId = s.droneId();
        e.imsi = s.imsi().value();
        e.serviceOfferingId = s.serviceOfferingId();
        for (Characteristic c : s.characteristics()) {
            switch (c.name()) {
                case "sliceType" -> e.sliceType = c.value();
                case "sst" -> e.sst = Integer.valueOf(c.value());
                case "sd" -> e.sd = c.value();
                case "dnn" -> e.dnn = c.value();
                case "5qi" -> e.fiveQi = Integer.valueOf(c.value());
                default -> { }
            }
        }
        e.state = s.state().name();
        e.acceptedAt = s.acceptedAt();
        e.activatedAt = s.activatedAt();
        e.failureReason = s.failureReason();
        return e;
    }

    private static Service toDomain(ServiceJpaEntity e) {
        List<Characteristic> chars = new ArrayList<>();
        if (e.sliceType != null) chars.add(new Characteristic("sliceType", e.sliceType));
        if (e.sst != null) chars.add(new Characteristic("sst", String.valueOf(e.sst)));
        if (e.sd != null) chars.add(new Characteristic("sd", e.sd));
        if (e.dnn != null) chars.add(new Characteristic("dnn", e.dnn));
        if (e.fiveQi != null) chars.add(new Characteristic("5qi", String.valueOf(e.fiveQi)));
        return new Service(e.id, e.orderId, ClientId.of(e.clientId), e.droneId, new Imsi(e.imsi), e.serviceOfferingId,
                chars, e.acceptedAt, ServiceState.valueOf(e.state), e.activatedAt, e.failureReason);
    }
}
