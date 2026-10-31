package com.candronex.order.domain;

import java.util.List;
import java.util.Optional;

/** Port de sortie : lecture du catalogue auprès de CatalogService (API REST). */
public interface ServiceCatalogue {

    record OfferingSnapshot(ServiceOfferingId id, List<Characteristic> characteristics) { }

    Optional<OfferingSnapshot> find(ServiceOfferingId id);
}
