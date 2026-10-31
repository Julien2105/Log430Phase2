package com.candronex.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(@NotEmpty @Valid List<ItemRequest> items) {

    public record ItemRequest(@NotBlank String action, @NotBlank String droneId, @NotBlank String serviceOfferingId) { }
}
