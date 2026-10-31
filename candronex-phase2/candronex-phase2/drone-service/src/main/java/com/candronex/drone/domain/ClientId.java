package com.candronex.drone.domain;

public record ClientId(String value) {

    public ClientId {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw DomainException.validation("VALIDATION_ERROR", "clientId absent ou invalide");
        }
    }

    public static ClientId of(String value) { return new ClientId(value); }

    @Override
    public String toString() { return value; }
}
