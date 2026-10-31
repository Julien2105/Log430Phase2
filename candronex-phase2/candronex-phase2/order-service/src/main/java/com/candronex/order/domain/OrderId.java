package com.candronex.order.domain;

import java.util.UUID;

public record OrderId(UUID value) {
    public static OrderId random() { return new OrderId(UUID.randomUUID()); }
    public static OrderId of(UUID value) { return new OrderId(value); }
    @Override public String toString() { return value.toString(); }
}
