package com.candronex.inventory.api;

import com.candronex.inventory.application.ConsultInventoryQuery;
import com.candronex.inventory.application.RecordServiceInstanceUseCase;
import com.candronex.inventory.domain.ClientId;
import com.candronex.inventory.domain.DomainException;
import com.candronex.inventory.domain.ServiceInstance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Inventaire des services")
public class InventoryController {

    private final RecordServiceInstanceUseCase record;
    private final ConsultInventoryQuery query;
    private final Set<String> trustedClients;

    public InventoryController(RecordServiceInstanceUseCase record, ConsultInventoryQuery query,
                               @Value("#{'${candronex.inventory.trusted-clients:}'.split(',')}") Set<String> trustedClients) {
        this.record = record;
        this.query = query;
        this.trustedClients = trustedClients;
    }

    @PutMapping("/{serviceId}")
    @Operation(summary = "Enregistrer un service activé (interne : appelé par slice-service)")
    public ServiceInstanceResponse put(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID serviceId,
                                       @Valid @RequestBody ServiceInstanceRequest body) {
        String caller = jwt.getClaimAsString("clientId");
        // Le client écrit pour lui-même; seul un client technique de confiance écrit au nom d'un client.
        if (!caller.equals(body.clientId()) && !trustedClients.contains(caller)) {
            throw DomainException.forbidden("Écriture interdite pour ce client");
        }
        ServiceInstance saved = record.record(new ServiceInstance(serviceId, ClientId.of(body.clientId()), body.orderId(),
                body.droneId(), body.serviceOfferingId(), body.state(), body.activatedAt()));
        return ServiceInstanceResponse.from(saved);
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "Consulter un service activé du client authentifié")
    public ServiceInstanceResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID serviceId) {
        return ServiceInstanceResponse.from(query.get(Clients.of(jwt), serviceId));
    }

    @GetMapping
    @Operation(summary = "Lister les services activés du client, filtrés par droneId")
    public List<ServiceInstanceResponse> list(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String droneId) {
        return query.list(Clients.of(jwt), droneId).stream().map(ServiceInstanceResponse::from).toList();
    }
}
