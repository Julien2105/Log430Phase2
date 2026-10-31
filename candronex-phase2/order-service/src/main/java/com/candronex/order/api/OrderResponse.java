package com.candronex.order.api;

import com.candronex.order.domain.OrderView;
import com.candronex.order.domain.OrderLine;
import java.time.Instant;
import java.util.List;

/** Représentation publique d'une commande : aucun IMSI. */
public record OrderResponse(String orderId, String state, Instant acceptedAt, List<ItemResponse> items) {

    public record CharacteristicDto(String name, String value) { }

    public record ServiceDto(String serviceOfferingId, List<CharacteristicDto> serviceCharacteristic) { }

    public record ItemResponse(String lineId, String action, String droneId, String state, ServiceDto service) { }

    public static OrderResponse from(OrderView v) {
        List<ItemResponse> items = v.order().lines().stream().map(l -> item(v, l)).toList();
        return new OrderResponse(v.order().id().toString(), v.state().name(), v.order().acceptedAt(), items);
    }

    private static ItemResponse item(OrderView v, OrderLine l) {
        List<CharacteristicDto> chars = l.characteristics().stream()
                .map(c -> new CharacteristicDto(c.name(), c.value())).toList();
        return new ItemResponse(l.id().toString(), l.action().name(), l.droneId().value(), v.stateOf(l.id()).name(),
                new ServiceDto(l.serviceOfferingId().value(), chars));
    }
}
