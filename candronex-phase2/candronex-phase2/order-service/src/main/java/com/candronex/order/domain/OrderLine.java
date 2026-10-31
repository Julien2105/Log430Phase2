package com.candronex.order.domain;

import java.util.List;

/** Ligne de commande : l'intention du client + copie (snapshot) des caractéristiques de l'offre. */
public record OrderLine(LineId id, ItemAction action, DroneId droneId, ServiceOfferingId serviceOfferingId,
                        List<Characteristic> characteristics) {
    public OrderLine {
        characteristics = List.copyOf(characteristics);
    }
}
