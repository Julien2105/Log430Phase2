package com.candronex.drone.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDroneRequest(
        @NotBlank @Size(max = 64) String droneId,
        @NotBlank String imsi,
        @Size(max = 200) String label) { }
