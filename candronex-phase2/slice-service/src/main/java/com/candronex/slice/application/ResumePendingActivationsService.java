package com.candronex.slice.application;

import com.candronex.slice.domain.ActivationScheduler;
import com.candronex.slice.domain.ActivationScheduler.ActivationJob;
import com.candronex.slice.domain.ServiceRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ResumePendingActivationsService implements ResumePendingActivationsUseCase {

    private final ServiceRepository services;
    private final ActivationScheduler scheduler;

    public ResumePendingActivationsService(ServiceRepository services, ActivationScheduler scheduler) {
        this.services = services;
        this.scheduler = scheduler;
    }

    @Override
    public int resume() {
        services.resetInterruptedActivations();
        List<UUID> pending = services.findPendingIds();
        pending.forEach(id -> scheduler.schedule(new ActivationJob(id, null, null)));
        return pending.size();
    }
}
