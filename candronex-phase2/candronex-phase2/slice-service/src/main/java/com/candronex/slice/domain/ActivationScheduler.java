package com.candronex.slice.domain;

import java.util.UUID;

/** Port de sortie : exécution asynchrone d'une activation (fils virtuels). */
public interface ActivationScheduler {

    record ActivationJob(UUID serviceId, String bearerToken, String correlationId) { }

    void schedule(ActivationJob job);
}
