package com.candronex.inventory.domain;

import java.time.Instant;
import java.util.UUID;

/** Réalité activée : un service effectivement actif sur le réseau. */
public record ServiceInstance(UUID serviceId, ClientId clientId, UUID orderId, String droneId,
                              String serviceOfferingId, String state, Instant activatedAt) { }
