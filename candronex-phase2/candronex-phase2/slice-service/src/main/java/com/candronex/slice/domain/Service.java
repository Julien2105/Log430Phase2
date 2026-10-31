package com.candronex.slice.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Agrégat Service (contexte Service Activation) : cycle de vie d'une activation de slice.
 * PENDING -> ACTIVATING -> ACTIVE | FAILED; reprise après incident : ACTIVATING -> PENDING.
 */
public final class Service {

    private final UUID serviceId;
    private final UUID orderId;
    private final ClientId clientId;
    private final String droneId;
    private final Imsi imsi;
    private final String serviceOfferingId;
    private final List<Characteristic> characteristics;
    private final Instant acceptedAt;
    private ServiceState state;
    private Instant activatedAt;
    private String failureReason;

    public Service(UUID serviceId, UUID orderId, ClientId clientId, String droneId, Imsi imsi, String serviceOfferingId,
                   List<Characteristic> characteristics, Instant acceptedAt, ServiceState state,
                   Instant activatedAt, String failureReason) {
        this.serviceId = serviceId;
        this.orderId = orderId;
        this.clientId = clientId;
        this.droneId = droneId;
        this.imsi = imsi;
        this.serviceOfferingId = serviceOfferingId;
        this.characteristics = List.copyOf(characteristics);
        this.acceptedAt = acceptedAt;
        this.state = state;
        this.activatedAt = activatedAt;
        this.failureReason = failureReason;
    }

    public static Service accept(UUID serviceId, UUID orderId, ClientId clientId, String droneId, Imsi imsi,
                                 String serviceOfferingId, List<Characteristic> characteristics, Instant acceptedAt) {
        return new Service(serviceId, orderId, clientId, droneId, imsi, serviceOfferingId, characteristics,
                acceptedAt, ServiceState.PENDING, null, null);
    }

    public void startActivation() {
        require(ServiceState.PENDING);
        state = ServiceState.ACTIVATING;
    }

    public void markActive(Instant now) {
        require(ServiceState.ACTIVATING);
        state = ServiceState.ACTIVE;
        activatedAt = now;
        failureReason = null;
    }

    public void markFailed(String reason) {
        require(ServiceState.ACTIVATING);
        state = ServiceState.FAILED;
        failureReason = reason;
    }

    /** Reprise : une activation interrompue (ACTIVATING) redevient PENDING pour être rejouée. */
    public void resume() {
        require(ServiceState.ACTIVATING);
        state = ServiceState.PENDING;
    }

    private void require(ServiceState expected) {
        if (state != expected) {
            throw DomainException.conflict("INVALID_STATE_TRANSITION",
                    "Transition interdite depuis l'état " + state + " (attendu : " + expected + ")");
        }
    }

    public UUID serviceId() { return serviceId; }
    public UUID orderId() { return orderId; }
    public ClientId clientId() { return clientId; }
    public String droneId() { return droneId; }
    public Imsi imsi() { return imsi; }
    public String serviceOfferingId() { return serviceOfferingId; }
    public List<Characteristic> characteristics() { return characteristics; }
    public Instant acceptedAt() { return acceptedAt; }
    public ServiceState state() { return state; }
    public Instant activatedAt() { return activatedAt; }
    public String failureReason() { return failureReason; }
}
