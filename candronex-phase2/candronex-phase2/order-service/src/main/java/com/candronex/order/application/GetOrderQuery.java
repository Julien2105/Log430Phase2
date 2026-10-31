package com.candronex.order.application;

import com.candronex.order.domain.ClientId;
import com.candronex.order.domain.OrderId;
import com.candronex.order.domain.OrderView;
import java.util.List;

public interface GetOrderQuery {

    OrderView get(ClientId clientId, OrderId orderId);

    List<OrderView> list(ClientId clientId);
}
