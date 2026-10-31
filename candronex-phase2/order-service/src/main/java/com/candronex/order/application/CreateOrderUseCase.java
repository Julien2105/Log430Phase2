package com.candronex.order.application;

import com.candronex.order.domain.ClientId;
import com.candronex.order.domain.IdempotencyKey;
import com.candronex.order.domain.OrderView;
import java.util.List;

/** UC-04 : prendre une commande de services de connectivité pour des drones. */
public interface CreateOrderUseCase {

    record ItemCommand(String action, String droneId, String serviceOfferingId) { }

    record Command(ClientId clientId, IdempotencyKey key, List<ItemCommand> items) { }

    record Result(OrderView order, boolean created) { }

    Result create(Command command);
}
