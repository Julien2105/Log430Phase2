package com.candronex.order.infrastructure;

import com.candronex.order.domain.Characteristic;
import com.candronex.order.domain.ClientId;
import com.candronex.order.domain.Clock;
import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.DroneId;
import com.candronex.order.domain.IdempotencyKey;
import com.candronex.order.domain.ItemAction;
import com.candronex.order.domain.LineId;
import com.candronex.order.domain.Order;
import com.candronex.order.domain.OrderId;
import com.candronex.order.domain.OrderLine;
import com.candronex.order.domain.OrderRepository;
import com.candronex.order.domain.ServiceOfferingId;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur JPA du port OrderRepository (base order_db). */
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository orders;
    private final IdempotencyKeyJpaRepository keys;
    private final EntityManager em;
    private final Clock clock;

    public JpaOrderRepository(OrderJpaRepository orders, IdempotencyKeyJpaRepository keys, EntityManager em, Clock clock) {
        this.orders = orders;
        this.keys = keys;
        this.em = em;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IdempotencyRecord> findByKey(ClientId clientId, IdempotencyKey key) {
        return keys.findById(new IdempotencyKeyId(clientId.value(), key.value()))
                .map(k -> new IdempotencyRecord(k.getRequestHash(), OrderId.of(k.getOrderId())));
    }

    @Override
    @Transactional
    public void save(Order order, IdempotencyKey key, String requestHash) {
        try {
            OrderJpaEntity entity = new OrderJpaEntity(order.id().value(), order.clientId().value(), order.acceptedAt());
            for (OrderLine l : order.lines()) {
                List<CharacteristicEmbeddable> chars = l.characteristics().stream()
                        .map(c -> new CharacteristicEmbeddable(c.name(), c.value())).toList();
                entity.getLines().add(new OrderLineJpaEntity(l.id().value(), entity, l.action().name(),
                        l.droneId().value(), l.serviceOfferingId().value(), chars));
            }
            em.persist(entity);
            em.persist(new IdempotencyKeyJpaEntity(order.clientId().value(), key.value(), requestHash,
                    order.id().value(), clock.now()));
            em.flush();
        } catch (DataIntegrityViolationException e) {
            // Requête concurrente avec la même clé : la clé primaire (client_id, idempotency_key) protège les données.
            throw DomainException.conflict("IDEMPOTENCY_KEY_CONFLICT", "Requête concurrente avec la même Idempotency-Key; réessayez");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> find(ClientId clientId, OrderId orderId) {
        return orders.findByIdAndClientId(orderId.value(), clientId.value()).map(JpaOrderRepository::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll(ClientId clientId) {
        return orders.findByClientIdOrderByAcceptedAtDesc(clientId.value()).stream().map(JpaOrderRepository::toDomain).toList();
    }

    private static Order toDomain(OrderJpaEntity e) {
        List<OrderLine> lines = e.getLines().stream().map(l -> new OrderLine(
                LineId.of(l.getId()), ItemAction.parse(l.getAction()), new DroneId(l.getDroneId()),
                new ServiceOfferingId(l.getServiceOfferingId()),
                l.getCharacteristics().stream().map(c -> new Characteristic(c.getName(), c.getValue())).toList())).toList();
        return new Order(OrderId.of(e.getId()), ClientId.of(e.getClientId()), e.getAcceptedAt(), lines);
    }
}
