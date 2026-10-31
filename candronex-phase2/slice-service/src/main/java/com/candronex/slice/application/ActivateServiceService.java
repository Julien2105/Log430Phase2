package com.candronex.slice.application;

import com.candronex.slice.domain.ActivationScheduler;
import com.candronex.slice.domain.ActivationScheduler.ActivationJob;
import com.candronex.slice.domain.AuditTrail;
import com.candronex.slice.domain.Clock;
import com.candronex.slice.domain.DomainException;
import com.candronex.slice.domain.NetworkProvisioningPort;
import com.candronex.slice.domain.NetworkProvisioningPort.ProvisioningRequest;
import com.candronex.slice.domain.NetworkProvisioningPort.ProvisioningResult;
import com.candronex.slice.domain.Service;
import com.candronex.slice.domain.ServiceInventory;
import com.candronex.slice.domain.ServiceInventory.ServiceInstanceRecord;
import com.candronex.slice.domain.ServiceRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ActivateServiceService implements ActivateServiceUseCase {

    private final ServiceRepository services;
    private final NetworkProvisioningPort provisioning;
    private final ServiceInventory inventory;
    private final ActivationScheduler scheduler;
    private final AuditTrail audit;
    private final Clock clock;

    public ActivateServiceService(ServiceRepository services, NetworkProvisioningPort provisioning, ServiceInventory inventory,
                                  ActivationScheduler scheduler, AuditTrail audit, Clock clock) {
        this.services = services;
        this.provisioning = provisioning;
        this.inventory = inventory;
        this.scheduler = scheduler;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public Result accept(Command c) {
        Optional<Service> existing = services.find(c.serviceId());
        if (existing.isPresent()) {
            if (!existing.get().clientId().equals(c.clientId())) {
                throw DomainException.conflict("SERVICE_CONFLICT", "Ce service appartient à un autre client");
            }
            return new Result(existing.get(), false);
        }
        Service service = Service.accept(c.serviceId(), c.orderId(), c.clientId(), c.droneId(), c.imsi(),
                c.serviceOfferingId(), c.characteristics(), c.acceptedAt());
        services.save(service);
        audit.record("SERVICE_ACCEPTED", c.clientId().value(), c.orderId().toString(), "serviceId=" + c.serviceId());
        scheduler.schedule(new ActivationJob(c.serviceId(), c.bearerToken(), c.correlationId()));
        return new Result(service, true);
    }

    @Override
    public void execute(ActivationJob job) {
        Optional<Service> claimed = services.claimForActivation(job.serviceId());
        if (claimed.isEmpty()) return;
        Service service = claimed.get();
        String client = service.clientId().value();
        String order = service.orderId().toString();
        audit.record("SERVICE_ACTIVATING", client, order, "serviceId=" + service.serviceId());

        ProvisioningResult result;
        try {
            result = provisioning.provision(new ProvisioningRequest(service.serviceId(), service.imsi(), service.characteristics()));
        } catch (RuntimeException e) {
            result = ProvisioningResult.failed("PROVISIONING_ERROR");
        }

        if (!result.success()) {
            service.markFailed(result.failureReason());
            services.save(service);
            audit.record("SERVICE_FAILED", client, order, "serviceId=" + service.serviceId() + " reason=" + result.failureReason());
            return;
        }
        service.markActive(clock.now());
        try {
            inventory.record(new ServiceInstanceRecord(service.serviceId(), service.clientId(), service.orderId(),
                    service.droneId(), service.serviceOfferingId(), service.activatedAt()), job.bearerToken());
        } catch (RuntimeException e) {
            audit.record("INVENTORY_SYNC_FAILED", client, order, "serviceId=" + service.serviceId() + " cause=" + e.getClass().getSimpleName());
        }
        services.save(service);
        audit.record("SERVICE_ACTIVATED", client, order, "serviceId=" + service.serviceId());
    }
}
