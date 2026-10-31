package com.candronex.order.domain;

import java.util.List;
import java.util.Optional;

/** Port de sortie : persistance de order_db (commandes, lignes, snapshot, clés d'idempotence). */
public interface OrderRepository {

    record IdempotencyRecord(String requestHash, OrderId orderId) { }

    Optional<IdempotencyRecord> findByKey(ClientId clientId, IdempotencyKey key);

    /** Écrit la commande et la clé d'idempotence dans une même transaction locale. */
    void save(Order order, IdempotencyKey key, String requestHash);

    Optional<Order> find(ClientId clientId, OrderId orderId);

    List<Order> findAll(ClientId clientId);
}
