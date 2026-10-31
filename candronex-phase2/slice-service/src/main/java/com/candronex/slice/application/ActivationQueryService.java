package com.candronex.slice.application;

import com.candronex.slice.domain.ClientId;
import com.candronex.slice.domain.Service;
import com.candronex.slice.domain.ServiceRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ActivationQueryService implements ConsultActivationQuery {

    private final ServiceRepository services;

    public ActivationQueryService(ServiceRepository services) { this.services = services; }

    @Override
    public List<Service> byOrder(ClientId clientId, UUID orderId) { return services.findByOrder(clientId, orderId); }
}
