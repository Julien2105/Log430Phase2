package com.candronex.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingRepository {

    List<ServiceOffering> findAll();

    Optional<ServiceOffering> find(ServiceOfferingId id);
}
