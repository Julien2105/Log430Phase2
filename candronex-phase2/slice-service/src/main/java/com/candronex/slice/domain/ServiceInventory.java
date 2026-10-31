package com.candronex.slice.domain;

import java.time.Instant;
import java.util.UUID;

/** Port de sortie : enregistrement du ServiceInstance activé dans InventoryService. */
public interface ServiceInventory {

    record ServiceInstanceRecord(UUID serviceId, ClientId clientId, UUID orderId, String droneId,
                                 String serviceOfferingId, Instant activatedAt) { }

    /** bearerToken : JWT du client si disponible, sinon jeton technique du service. */
    void record(ServiceInstanceRecord record, String bearerToken);
}
