package com.candronex.order.domain;

/** Port de sortie : vérifie la propriété du drone et lit son identité réseau auprès de DroneService (API REST). */
public interface DroneDirectory {

    record NetworkIdentity(DroneId droneId, String imsi) { }

    /** Lève NOT_FOUND si le drone n'existe pas ou n'appartient pas au client de l'appel. */
    NetworkIdentity networkIdentity(DroneId droneId);
}
