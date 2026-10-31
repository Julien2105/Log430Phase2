package com.candronex.catalog.application;

import com.candronex.catalog.domain.ServiceOffering;
import com.candronex.catalog.domain.ServiceOfferingId;
import java.util.List;

public interface ConsultCatalogQuery {

    List<ServiceOffering> list();

    ServiceOffering get(ServiceOfferingId id);
}
