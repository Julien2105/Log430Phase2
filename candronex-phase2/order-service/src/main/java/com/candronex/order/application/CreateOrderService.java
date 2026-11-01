package com.candronex.order.application;

import com.candronex.order.domain.AuditTrail;
import com.candronex.order.domain.ClientId;
import com.candronex.order.domain.IdempotencyKey;
import com.candronex.order.domain.Characteristic;
import com.candronex.order.domain.Clock;
import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.DroneDirectory;
import com.candronex.order.domain.DroneDirectory.NetworkIdentity;
import com.candronex.order.domain.DroneId;
import com.candronex.order.domain.ItemAction;
import com.candronex.order.domain.LineId;
import com.candronex.order.domain.Order;
import com.candronex.order.domain.OrderId;
import com.candronex.order.domain.OrderLine;
import com.candronex.order.domain.OrderRepository;
import com.candronex.order.domain.OrderRepository.IdempotencyRecord;
import com.candronex.order.domain.OrderView;
import com.candronex.order.domain.ServiceActivationGateway;
import com.candronex.order.domain.ServiceActivationGateway.ActivationRequest;
import com.candronex.order.domain.ServiceCatalogue;
import com.candronex.order.domain.ServiceCatalogue.OfferingSnapshot;
import com.candronex.order.domain.ServiceOfferingId;
import com.candronex.order.domain.ServiceState;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateOrderService implements CreateOrderUseCase, GetOrderQuery {

    private static final Logger LOG = LoggerFactory.getLogger(CreateOrderService.class);
    private static final int MAX_ITEMS = 50;

    private final OrderRepository orders;
    private final DroneDirectory drones;
    private final ServiceCatalogue catalogue;
    private final ServiceActivationGateway activation;
    private final AuditTrail audit;
    private final Clock clock;

    public CreateOrderService(OrderRepository orders, DroneDirectory drones, ServiceCatalogue catalogue,
                              ServiceActivationGateway activation, AuditTrail audit, Clock clock) {
        this.orders = orders;
        this.drones = drones;
        this.catalogue = catalogue;
        this.activation = activation;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public Result create(Command c) {
        if (c.items() == null || c.items().isEmpty() || c.items().size() > MAX_ITEMS) {
            throw DomainException.validation("VALIDATION_ERROR", "La commande doit contenir entre 1 et " + MAX_ITEMS + " éléments");
        }
        String hash = hash(c.items());
        Optional<IdempotencyRecord> known = orders.findByKey(c.clientId(), c.key());
        if (known.isPresent()) {
            if (!known.get().requestHash().equals(hash)) {
                throw DomainException.conflict("IDEMPOTENCY_KEY_CONFLICT",
                        "Cette Idempotency-Key a déjà été utilisée avec un contenu différent");
            }
            Order existing = orders.find(c.clientId(), known.get().orderId())
                    .orElseThrow(() -> DomainException.notFound("Commande introuvable"));
            audit.record("ORDER_REPLAYED", c.clientId().value(), existing.id().toString(), "key=" + c.key());
            completeDispatch(existing, new LinkedHashMap<>(), true);
            return new Result(view(existing), false);
        }
        Map<DroneId, NetworkIdentity> identities = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        List<OrderLine> lines = new ArrayList<>();
        for (ItemCommand item : c.items()) {
            ItemAction action = ItemAction.parse(item.action());
            DroneId droneId = new DroneId(item.droneId());
            ServiceOfferingId offeringId = new ServiceOfferingId(item.serviceOfferingId());
            if (!seen.add(droneId.value() + "|" + offeringId.value())) {
                throw DomainException.unprocessable("UNSUPPORTED_ITEM", "Ligne dupliquée dans la commande");
            }
            if (!identities.containsKey(droneId)) identities.put(droneId, drones.networkIdentity(droneId));
            OfferingSnapshot offering = catalogue.find(offeringId)
                    .orElseThrow(() -> DomainException.unprocessable("UNSUPPORTED_ITEM", "Offre de service inconnue : " + offeringId));
            lines.add(new OrderLine(LineId.random(), action, droneId, offeringId, offering.characteristics()));
        }
        Order order = new Order(OrderId.random(), c.clientId(), clock.now(), lines);
        orders.save(order, c.key(), hash);
        audit.record("ORDER_CREATED", c.clientId().value(), order.id().toString(), "lines=" + lines.size());
        completeDispatch(order, identities, false);
        return new Result(new OrderView(order, Map.of()), true);
    }
    private void completeDispatch(Order order, Map<DroneId, NetworkIdentity> identities, boolean replay) {
        Map<LineId, ServiceState> known = Map.of();
        if (replay) {
            try {
                known = activation.statesOf(order.id());
            } catch (DomainException e) {
                known = Map.of();
            }
        }
        for (OrderLine line : order.lines()) {
            if (known.containsKey(line.id())) continue;
            try {
                NetworkIdentity identity = identities.get(line.droneId());
                if (identity == null) {
                    identity = drones.networkIdentity(line.droneId());
                    identities.put(line.droneId(), identity);
                }
                activation.requestActivation(new ActivationRequest(line.id(), order.id(), line.droneId(), identity.imsi(),
                        line.serviceOfferingId(), line.characteristics(), order.acceptedAt()));
            } catch (DomainException e) {
                LOG.warn("event=ACTIVATION_DISPATCH_FAILED lineId={} code={}", line.id(), e.code());
            }
        }
    }

    private OrderView view(Order order) {
        try {
            return new OrderView(order, activation.statesOf(order.id()));
        } catch (DomainException e) {
            return new OrderView(order, Map.of());
        }
    }

    @Override
    public OrderView get(ClientId clientId, OrderId orderId) {
        Order order = orders.find(clientId, orderId).orElseThrow(() -> DomainException.notFound("Commande introuvable"));
        return view(order);
    }

    @Override
    public List<OrderView> list(ClientId clientId) {
        return orders.findAll(clientId).stream().map(this::view).toList();
    }

    private static String hash(List<ItemCommand> items) {
        List<String> canonical = items.stream()
                .map(i -> String.valueOf(i.action()).toUpperCase() + "|" + i.droneId() + "|" + i.serviceOfferingId())
                .sorted().toList();
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(String.join(";", canonical).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
