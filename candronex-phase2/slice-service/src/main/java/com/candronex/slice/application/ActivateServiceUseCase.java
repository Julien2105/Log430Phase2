package com.candronex.slice.application;

import com.candronex.slice.domain.ActivationScheduler.ActivationJob;
import com.candronex.slice.domain.Characteristic;
import com.candronex.slice.domain.ClientId;
import com.candronex.slice.domain.Imsi;
import com.candronex.slice.domain.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** UC-06 : cas d'utilisation principal de SliceService. */
public interface ActivateServiceUseCase {

    record Command(UUID serviceId, UUID orderId, ClientId clientId, String droneId, Imsi imsi, String serviceOfferingId,
                   List<Characteristic> characteristics, Instant acceptedAt, String bearerToken, String correlationId) { }

    record Result(Service service, boolean created) { }

    /** Accepte la demande (202) : idempotent par serviceId; l'activation réseau est asynchrone. */
    Result accept(Command command);

    /** Exécute l'activation : PENDING -> ACTIVATING -> ACTIVE | FAILED, puis alimente l'inventaire. */
    void execute(ActivationJob job);
}
