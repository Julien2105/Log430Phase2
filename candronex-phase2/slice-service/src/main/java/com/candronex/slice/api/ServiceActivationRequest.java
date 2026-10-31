package com.candronex.slice.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Contrat interne order-service -> slice-service (non publié par la passerelle). */
public record ServiceActivationRequest(
        @NotNull UUID serviceId,
        @NotNull UUID orderId,
        @NotBlank String droneId,
        @NotBlank String imsi,
        @NotBlank String serviceOfferingId,
        @NotEmpty @Valid List<CharacteristicDto> serviceCharacteristic,
        @NotNull Instant acceptedAt) {

    public record CharacteristicDto(@NotBlank String name, @NotBlank String value) { }
}
