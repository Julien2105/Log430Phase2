package com.candronex.order.domain;

import java.time.Instant;
import java.util.List;

/** Agrégat Order : exprime ce que le client demande. N'écrit jamais dans l'inventaire. */
public record Order(OrderId id, ClientId clientId, Instant acceptedAt, List<OrderLine> lines) {
    public Order {
        lines = List.copyOf(lines);
    }
}
