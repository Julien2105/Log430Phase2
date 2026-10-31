package com.candronex.order.domain;

public record ServiceOfferingId(String value) {

    public ServiceOfferingId {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw DomainException.validation("VALIDATION_ERROR", "serviceOfferingId invalide");
        }
    }

    @Override public String toString() { return value; }
}
