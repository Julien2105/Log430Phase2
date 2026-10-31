package com.candronex.slice.infrastructure;

import com.candronex.slice.application.ActivateServiceUseCase;
import com.candronex.slice.domain.ActivationScheduler;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Exécute chaque activation sur un fil virtuel : la requête HTTP répond 202 sans attendre le réseau. */
@Component
public class VirtualThreadActivationScheduler implements ActivationScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(VirtualThreadActivationScheduler.class);

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ObjectProvider<ActivateServiceUseCase> useCase; // évite la dépendance circulaire

    public VirtualThreadActivationScheduler(ObjectProvider<ActivateServiceUseCase> useCase) { this.useCase = useCase; }

    @Override
    public void schedule(ActivationJob job) {
        executor.submit(() -> {
            if (job.correlationId() != null) MDC.put("correlationId", job.correlationId());
            try {
                useCase.getObject().execute(job);
            } catch (RuntimeException e) {
                LOG.error("event=ACTIVATION_ERROR serviceId={}", job.serviceId(), e);
            } finally {
                MDC.clear();
            }
        });
    }

    @PreDestroy
    void shutdown() { executor.shutdown(); }
}
