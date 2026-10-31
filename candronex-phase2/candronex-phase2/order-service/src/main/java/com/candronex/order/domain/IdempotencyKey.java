package com.candronex.order.domain;

public record IdempotencyKey(String value) {

    public IdempotencyKey {
        if (value == null || value.isBlank() || value.length() > 128) {
            throw DomainException.validation("VALIDATION_ERROR", "En-tête Idempotency-Key absent ou invalide");
        }
    }

    @Override public String toString() { return value; }
}
