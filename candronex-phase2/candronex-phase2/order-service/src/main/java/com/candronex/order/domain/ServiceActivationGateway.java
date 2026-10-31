package com.candronex.order.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Port de sortie : UC-06 auprès de SliceService (API REST uniquement, jamais son dépôt ni ses entités). */
public interface ServiceActivationGateway {

    record ActivationRequest(LineId serviceId, OrderId orderId, DroneId droneId, String imsi,
                             ServiceOfferingId serviceOfferingId, List<Characteristic> characteristics, Instant acceptedAt) { }

    void requestActivation(ActivationRequest request);

    Map<LineId, ServiceState> statesOf(OrderId orderId);
}
