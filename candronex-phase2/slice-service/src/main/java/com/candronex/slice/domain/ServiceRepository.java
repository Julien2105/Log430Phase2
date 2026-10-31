package com.candronex.slice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie : persistance des Service dans slice_db. */
public interface ServiceRepository {

    Optional<Service> find(UUID serviceId);

    List<Service> findByOrder(ClientId clientId, UUID orderId);

    void save(Service service);

    /** Passage atomique PENDING -> ACTIVATING (une seule activation concurrente gagne). */
    Optional<Service> claimForActivation(UUID serviceId);

    /** Reprise : ACTIVATING -> PENDING pour toutes les activations interrompues. */
    int resetInterruptedActivations();

    List<UUID> findPendingIds();
}
