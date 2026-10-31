package com.candronex.order.domain;

public enum ItemAction {
    ADD;

    public static ItemAction parse(String raw) {
        if (raw != null && raw.equalsIgnoreCase("ADD")) return ADD;
        throw DomainException.unprocessable("UNSUPPORTED_ITEM", "Action non supportée : seule ADD est acceptée en Phase 2");
    }
}
