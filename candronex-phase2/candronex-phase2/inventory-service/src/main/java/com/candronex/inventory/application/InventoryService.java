package com.candronex.inventory.application;

import com.candronex.inventory.domain.AuditTrail;
import com.candronex.inventory.domain.ClientId;
import com.candronex.inventory.domain.DomainException;
import com.candronex.inventory.domain.ServiceInstance;
import com.candronex.inventory.domain.ServiceInventoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InventoryService implements RecordServiceInstanceUseCase, ConsultInventoryQuery {

    private final ServiceInventoryRepository repository;
    private final AuditTrail audit;

    public InventoryService(ServiceInventoryRepository repository, AuditTrail audit) {
        this.repository = repository;
        this.audit = audit;
    }

    @Override
    public ServiceInstance record(ServiceInstance instance) {
        if (!"ACTIVE".equals(instance.state())) {
            throw DomainException.unprocessable("UNSUPPORTED_ITEM", "Seuls les services ACTIVE sont enregistrés dans l'inventaire");
        }
        ServiceInstance saved = repository.upsert(instance);
        audit.record("INVENTORY_RECORDED", instance.clientId().value(), instance.orderId().toString(), "serviceId=" + instance.serviceId());
        return saved;
    }

    @Override
    public ServiceInstance get(ClientId clientId, UUID serviceId) {
        return repository.find(clientId, serviceId).orElseThrow(() -> DomainException.notFound("Service introuvable"));
    }

    @Override
    public List<ServiceInstance> list(ClientId clientId, String droneId) {
        return droneId == null || droneId.isBlank() ? repository.findAll(clientId) : repository.findByDrone(clientId, droneId);
    }
}
