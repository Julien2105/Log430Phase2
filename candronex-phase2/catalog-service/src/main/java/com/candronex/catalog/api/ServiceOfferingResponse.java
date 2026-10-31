package com.candronex.catalog.api;

import com.candronex.catalog.domain.ServiceOffering;
import java.util.List;

public record ServiceOfferingResponse(
        String serviceOfferingId,
        String name,
        String description,
        SpecificationDto serviceSpecification,
        List<CharacteristicDto> serviceCharacteristics) {

    public record SpecificationDto(String id, String name) { }

    public record CharacteristicDto(String name, String value) { }

    public static ServiceOfferingResponse from(ServiceOffering o) {
        return new ServiceOfferingResponse(
                o.id().value(), o.name(), o.description(),
                new SpecificationDto(o.specification().id(), o.specification().name()),
                o.specification().characteristics().stream()
                        .map(c -> new CharacteristicDto(c.name(), c.value())).toList());
    }
}
