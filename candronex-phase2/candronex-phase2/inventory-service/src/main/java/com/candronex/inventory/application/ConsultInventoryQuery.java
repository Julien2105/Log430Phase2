package com.candronex.inventory.application;

import com.candronex.inventory.domain.ClientId;
import com.candronex.inventory.domain.ServiceInstance;
import java.util.List;
import java.util.UUID;

public interface ConsultInventoryQuery {

    ServiceInstance get(ClientId clientId, UUID serviceId);

    List<ServiceInstance> list(ClientId clientId, String droneId);
}
