package com.candronex.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.candronex.order.application.CreateOrderService;
import com.candronex.order.application.CreateOrderUseCase.Command;
import com.candronex.order.application.CreateOrderUseCase.ItemCommand;
import com.candronex.order.domain.Characteristic;
import com.candronex.order.domain.ClientId;
import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.DroneDirectory;
import com.candronex.order.domain.DroneDirectory.NetworkIdentity;
import com.candronex.order.domain.DroneId;
import com.candronex.order.domain.IdempotencyKey;
import com.candronex.order.domain.LineId;
import com.candronex.order.domain.Order;
import com.candronex.order.domain.OrderId;
import com.candronex.order.domain.OrderRepository;
import com.candronex.order.domain.OrderState;
import com.candronex.order.domain.ServiceActivationGateway;
import com.candronex.order.domain.ServiceActivationGateway.ActivationRequest;
import com.candronex.order.domain.ServiceCatalogue;
import com.candronex.order.domain.ServiceCatalogue.OfferingSnapshot;
import com.candronex.order.domain.ServiceOfferingId;
import com.candronex.order.domain.ServiceState;
import com.candronex.order.support.FixedClock;
import com.candronex.order.support.RecordingAuditTrail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** UC-04 : prise de commande, snapshot, idempotence, propriété des drones, dégradation si SliceService est indisponible. */
class CreateOrderServiceTest {

    static class InMemoryOrders implements OrderRepository {
        final Map<String, IdempotencyRecord> keys = new HashMap<>();
        final Map<OrderId, Order> orders = new HashMap<>();
        @Override public Optional<IdempotencyRecord> findByKey(ClientId c, IdempotencyKey k) { return Optional.ofNullable(keys.get(c + "|" + k)); }
        @Override public void save(Order o, IdempotencyKey k, String hash) {
            orders.put(o.id(), o);
            keys.put(o.clientId() + "|" + k, new IdempotencyRecord(hash, o.id()));
        }
        @Override public Optional<Order> find(ClientId c, OrderId id) {
            return Optional.ofNullable(orders.get(id)).filter(o -> o.clientId().equals(c));
        }
        @Override public List<Order> findAll(ClientId c) { return orders.values().stream().filter(o -> o.clientId().equals(c)).toList(); }
    }

    static class FakeSlice implements ServiceActivationGateway {
        final List<ActivationRequest> requests = new ArrayList<>();
        final Map<LineId, ServiceState> states = new HashMap<>();
        boolean down;
        @Override public void requestActivation(ActivationRequest r) {
            if (down) throw DomainException.unavailable("slice down");
            requests.add(r);
            states.put(r.serviceId(), ServiceState.PENDING);
        }
        @Override public Map<LineId, ServiceState> statesOf(OrderId id) {
            if (down) throw DomainException.unavailable("slice down");
            return states;
        }
    }

    private static final ClientId A = ClientId.of("client-a");
    private static final ClientId B = ClientId.of("client-b");

    private InMemoryOrders orders;
    private FakeSlice slice;
    private Map<String, String> ownedDrones; // droneId -> clientId
    private Map<String, List<Characteristic>> catalogue;
    private CreateOrderService service;
    private ClientId caller = A;

    @BeforeEach
    void setUp() {
        orders = new InMemoryOrders();
        slice = new FakeSlice();
        ownedDrones = new HashMap<>(Map.of("DRN-0231", "client-a"));
        catalogue = new HashMap<>(Map.of(
                "C2_URLLC", List.of(new Characteristic("sst", "2"), new Characteristic("5qi", "82")),
                "IMAGERY_EMBB", List.of(new Characteristic("sst", "1"), new Characteristic("5qi", "9"))));
        DroneDirectory drones = id -> {
            if (!caller.value().equals(ownedDrones.get(id.value()))) throw DomainException.notFound("Drone introuvable");
            return new NetworkIdentity(id, "302720123456789");
        };
        ServiceCatalogue cat = id -> Optional.ofNullable(catalogue.get(id.value())).map(c -> new OfferingSnapshot(id, c));
        service = new CreateOrderService(orders, drones, cat, slice, new RecordingAuditTrail(), new FixedClock());
    }

    private Command cmd(String key, ItemCommand... items) { return new Command(caller, new IdempotencyKey(key), List.of(items)); }

    private ItemCommand item(String offering) { return new ItemCommand("ADD", "DRN-0231", offering); }

    @Test
    void createsTheOrderAndRequestsOneActivationPerLine() {
        var r = service.create(cmd("k1", item("C2_URLLC"), item("IMAGERY_EMBB")));
        assertThat(r.created()).isTrue();
        assertThat(r.order().order().lines()).hasSize(2);
        assertThat(slice.requests).hasSize(2);
        assertThat(slice.requests.get(0).serviceId()).isEqualTo(r.order().order().lines().get(0).id()); // serviceId = lineId
        assertThat(r.order().state()).isEqualTo(OrderState.ACCEPTED);
    }

    @Test
    void catalogueCharacteristicsAreCopiedIntoTheOrderLine() {
        var r = service.create(cmd("k2", item("C2_URLLC")));
        catalogue.put("C2_URLLC", List.of(new Characteristic("5qi", "1"))); // le catalogue change ensuite
        var line = service.get(A, r.order().order().id()).order().lines().get(0);
        assertThat(line.characteristics()).containsExactly(new Characteristic("sst", "2"), new Characteristic("5qi", "82"));
    }

    @Test
    void sameKeyAndSameContentReturnsTheSameOrder() {
        var first = service.create(cmd("k3", item("C2_URLLC")));
        var replay = service.create(cmd("k3", item("C2_URLLC")));
        assertThat(replay.created()).isFalse();
        assertThat(replay.order().order().id()).isEqualTo(first.order().order().id());
        assertThat(orders.orders).hasSize(1);
        assertThat(slice.requests).hasSize(1); // aucune nouvelle activation
    }

    @Test
    void sameKeyWithDifferentContentIsAConflict() {
        service.create(cmd("k4", item("C2_URLLC")));
        assertThatThrownBy(() -> service.create(cmd("k4", item("IMAGERY_EMBB"))))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.code()).isEqualTo("IDEMPOTENCY_KEY_CONFLICT"));
    }

    @Test
    void anotherClientsDroneIsNotFound() {
        caller = B;
        assertThatThrownBy(() -> service.create(cmd("k5", item("C2_URLLC"))))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.kind()).isEqualTo(DomainException.Kind.NOT_FOUND));
        assertThat(orders.orders).isEmpty();
    }

    @Test
    void unknownOfferingIsUnprocessable() {
        assertThatThrownBy(() -> service.create(cmd("k6", item("NOPE"))))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.code()).isEqualTo("UNSUPPORTED_ITEM"));
    }

    @Test
    void unsupportedActionIsUnprocessable() {
        assertThatThrownBy(() -> service.create(cmd("k7", new ItemCommand("REMOVE", "DRN-0231", "C2_URLLC"))))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.code()).isEqualTo("UNSUPPORTED_ITEM"));
    }

    @Test
    void sliceServiceDownLeavesTheOrderAcceptedAndReplayCompletesIt() {
        slice.down = true;
        var r = service.create(cmd("k8", item("C2_URLLC")));
        assertThat(r.created()).isTrue();
        assertThat(r.order().state()).isEqualTo(OrderState.ACCEPTED);
        assertThat(slice.requests).isEmpty();

        slice.down = false;
        var replay = service.create(cmd("k8", item("C2_URLLC")));
        assertThat(replay.created()).isFalse();
        assertThat(slice.requests).hasSize(1); // le rejeu avec la même clé termine le travail
    }

    @Test
    void emptyOrderIsRejected() {
        assertThatThrownBy(() -> service.create(new Command(A, new IdempotencyKey("k9"), List.of())))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void ordersAreListedPerClient() {
        service.create(cmd("k10", item("C2_URLLC")));
        assertThat(service.list(A)).hasSize(1);
        assertThat(service.list(B)).isEmpty();
    }

    @Test
    void globalStateFollowsTheServiceStatesReadFromSlice() {
        var r = service.create(cmd("k11", item("C2_URLLC"), item("IMAGERY_EMBB")));
        var ids = r.order().order().lines().stream().map(l -> l.id()).toList();
        slice.states.put(ids.get(0), ServiceState.ACTIVE);
        slice.states.put(ids.get(1), ServiceState.FAILED);
        assertThat(service.get(A, r.order().order().id()).state()).isEqualTo(OrderState.PARTIALLY_ACTIVE);
    }
}
