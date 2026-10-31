package com.candronex.catalog.domain;

public record ServiceOfferingId(String value) {

    public ServiceOfferingId {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw DomainException.validation("VALIDATION_ERROR", "serviceOfferingId invalide");
        }
    }

    public static ServiceOfferingId of(String value) { return new ServiceOfferingId(value); }
}
