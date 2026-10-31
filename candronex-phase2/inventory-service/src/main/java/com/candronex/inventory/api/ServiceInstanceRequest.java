package com.candronex.inventory.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/** Contrat interne slice-service -> inventory-service (PUT, non publié par la passerelle). */
public record ServiceInstanceRequest(
        @NotBlank String clientId,
        @NotNull UUID orderId,
        @NotBlank String droneId,
        @NotBlank String serviceOfferingId,
        @NotBlank String state,
        @NotNull Instant activatedAt) { }
