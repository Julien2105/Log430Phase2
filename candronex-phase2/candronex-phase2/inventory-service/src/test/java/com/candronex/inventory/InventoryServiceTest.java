package com.candronex.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.candronex.inventory.application.InventoryService;
import com.candronex.inventory.domain.ClientId;
import com.candronex.inventory.domain.DomainException;
import com.candronex.inventory.domain.ServiceInstance;
import com.candronex.inventory.domain.ServiceInventoryRepository;
import com.candronex.inventory.support.RecordingAuditTrail;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    private final List<ServiceInstance> store = new ArrayList<>();
    private final ServiceInventoryRepository repo = new ServiceInventoryRepository() {
        @Override public ServiceInstance upsert(ServiceInstance i) {
            store.removeIf(x -> x.serviceId().equals(i.serviceId()));
            store.add(i);
            return i;
        }
        @Override public Optional<ServiceInstance> find(ClientId c, UUID id) {
            return store.stream().filter(x -> x.serviceId().equals(id) && x.clientId().equals(c)).findFirst();
        }
        @Override public List<ServiceInstance> findAll(ClientId c) { return store.stream().filter(x -> x.clientId().equals(c)).toList(); }
        @Override public List<ServiceInstance> findByDrone(ClientId c, String d) {
            return findAll(c).stream().filter(x -> x.droneId().equals(d)).toList();
        }
    };
    private final InventoryService service = new InventoryService(repo, new RecordingAuditTrail());

    private ServiceInstance instance(UUID id, String client, String state) {
        return new ServiceInstance(id, ClientId.of(client), UUID.randomUUID(), "DRN-0231", "C2_URLLC", state, Instant.now());
    }

    @Test
    void recordIsIdempotent() {
        UUID id = UUID.randomUUID();
        service.record(instance(id, "client-a", "ACTIVE"));
        service.record(instance(id, "client-a", "ACTIVE"));
        assertThat(store).hasSize(1);
    }

    @Test
    void onlyActiveServicesEnterTheInventory() {
        assertThatThrownBy(() -> service.record(instance(UUID.randomUUID(), "client-a", "FAILED")))
                .isInstanceOf(DomainException.class);
        assertThat(store).isEmpty();
    }

    @Test
    void anotherClientCannotReadTheService() {
        UUID id = UUID.randomUUID();
        service.record(instance(id, "client-a", "ACTIVE"));
        assertThatThrownBy(() -> service.get(ClientId.of("client-b"), id))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.kind()).isEqualTo(DomainException.Kind.NOT_FOUND));
    }

    @Test
    void listCanBeFilteredByDrone() {
        service.record(instance(UUID.randomUUID(), "client-a", "ACTIVE"));
        assertThat(service.list(ClientId.of("client-a"), "DRN-0231")).hasSize(1);
        assertThat(service.list(ClientId.of("client-a"), "DRN-9999")).isEmpty();
    }
}
