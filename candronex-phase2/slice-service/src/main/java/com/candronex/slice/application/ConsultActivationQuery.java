package com.candronex.slice.application;

import com.candronex.slice.domain.ClientId;
import com.candronex.slice.domain.Service;
import java.util.List;
import java.util.UUID;

public interface ConsultActivationQuery {

    List<Service> byOrder(ClientId clientId, UUID orderId);
}
