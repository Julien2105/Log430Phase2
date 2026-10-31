package com.candronex.catalog.api;

import com.candronex.catalog.application.ConsultCatalogQuery;
import com.candronex.catalog.domain.ServiceOfferingId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/service-offerings")
@Tag(name = "Catalogue")
public class ServiceOfferingController {

    private final ConsultCatalogQuery catalog;

    public ServiceOfferingController(ConsultCatalogQuery catalog) { this.catalog = catalog; }

    @GetMapping
    @Operation(summary = "Lister les offres de service et leurs caractéristiques réseau")
    public List<ServiceOfferingResponse> list() {
        return catalog.list().stream().map(ServiceOfferingResponse::from).toList();
    }

    @GetMapping("/{serviceOfferingId}")
    @Operation(summary = "Consulter une offre de service")
    public ServiceOfferingResponse get(@PathVariable String serviceOfferingId) {
        return ServiceOfferingResponse.from(catalog.get(ServiceOfferingId.of(serviceOfferingId)));
    }
}
