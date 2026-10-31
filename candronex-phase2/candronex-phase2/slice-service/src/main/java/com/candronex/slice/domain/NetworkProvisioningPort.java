package com.candronex.slice.domain;

import java.util.List;
import java.util.UUID;

/**
 * Port de sortie : demande de configuration d'une slice auprès du réseau 5G.
 * Phase 2 : MockOpen5GSAdapter. Phase 3 : Open5GSAdapter, sans modification du domaine.
 */
public interface NetworkProvisioningPort {

    record ProvisioningRequest(UUID serviceId, Imsi imsi, List<Characteristic> characteristics) { }

    record ProvisioningResult(boolean success, String failureReason) {
        public static ProvisioningResult ok() { return new ProvisioningResult(true, null); }
        public static ProvisioningResult failed(String reason) { return new ProvisioningResult(false, reason); }
    }

    ProvisioningResult provision(ProvisioningRequest request);
}
