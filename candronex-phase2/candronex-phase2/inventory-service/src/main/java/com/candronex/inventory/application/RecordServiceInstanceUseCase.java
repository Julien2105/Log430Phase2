package com.candronex.inventory.application;

import com.candronex.inventory.domain.ServiceInstance;

public interface RecordServiceInstanceUseCase {

    /** Enregistrement idempotent (PUT) : rejouer produit le même état. */
    ServiceInstance record(ServiceInstance instance);
}
