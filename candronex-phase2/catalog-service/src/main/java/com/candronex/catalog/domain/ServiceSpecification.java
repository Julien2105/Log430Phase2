package com.candronex.catalog.domain;

import java.util.List;

public record ServiceSpecification(String id, String name, List<ServiceCharacteristic> characteristics) { }
