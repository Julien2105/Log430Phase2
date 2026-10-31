package com.candronex.catalog.application;

import com.candronex.catalog.domain.DomainException;
import com.candronex.catalog.domain.ServiceOffering;
import com.candronex.catalog.domain.ServiceOfferingId;
import com.candronex.catalog.domain.ServiceOfferingRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConsultCatalogService implements ConsultCatalogQuery {

    private final ServiceOfferingRepository offerings;

    public ConsultCatalogService(ServiceOfferingRepository offerings) { this.offerings = offerings; }

    @Override
    public List<ServiceOffering> list() { return offerings.findAll(); }

    @Override
    public ServiceOffering get(ServiceOfferingId id) {
        return offerings.find(id).orElseThrow(() -> DomainException.notFound("Offre de service introuvable"));
    }
}
