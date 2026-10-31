package com.candronex.catalog.domain;

/** Offre vendable au client : décrite par une ServiceSpecification et ses ServiceCharacteristic. */
public record ServiceOffering(ServiceOfferingId id, String name, String description, ServiceSpecification specification) { }
