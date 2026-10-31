package com.candronex.order.domain;

import java.util.UUID;

public record LineId(UUID value) {
    public static LineId random() { return new LineId(UUID.randomUUID()); }
    public static LineId of(UUID value) { return new LineId(value); }
    @Override public String toString() { return value.toString(); }
}
