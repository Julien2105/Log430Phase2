package com.candronex.slice;

import static org.assertj.core.api.Assertions.assertThat;

import com.candronex.slice.application.ActivateServiceService;
import com.candronex.slice.domain.Characteristic;
import com.candronex.slice.domain.ClientId;
import com.candronex.slice.domain.Imsi;
import com.candronex.slice.domain.Service;
import com.candronex.slice.domain.ServiceState;
import com.candronex.slice.application.ActivateServiceUseCase.Command;
import com.candronex.slice.application.ResumePendingActivationsService;
import com.candronex.slice.domain.ActivationScheduler;
import com.candronex.slice.domain.ActivationScheduler.ActivationJob;
import com.candronex.slice.domain.NetworkProvisioningPort;
import com.candronex.slice.domain.NetworkProvisioningPort.ProvisioningResult;
import com.candronex.slice.domain.ServiceInventory;
import com.candronex.slice.domain.ServiceInventory.ServiceInstanceRecord;
import com.candronex.slice.domain.ServiceRepository;
import com.candronex.slice.support.FixedClock;
import com.candronex.slice.support.RecordingAuditTrail;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** UC-06 : activation réussie, échouée, idempotente, concurrente et reprise (avec ports simulés en mémoire). */
class ActivateServiceServiceTest {

    static class InMemoryServices implements ServiceRepository {
        final Map<UUID, Service> store = new HashMap<>();
        @Override public Optional<Service> find(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Service> findByOrder(ClientId c, UUID o) {
            return store.values().stream().filter(s -> s.clientId().equals(c) && s.orderId().equals(o)).toList();
        }
        @Override public void save(Service s) { store.put(s.serviceId(), s); }
        @Override public Optional<Service> claimForActivation(UUID id) {
            Service s = store.get(id);
            if (s == null || s.state() != ServiceState.PENDING) return Optional.empty();
            s.startActivation();
            return Optional.of(s);
        }
        @Override public int resetInterruptedActivations() {
            int n = 0;
            for (Service s : store.values()) if (s.state() == ServiceState.ACTIVATING) { s.resume(); n++; }
            return n;
        }
        @Override public List<UUID> findPendingIds() {
            return store.values().stream().filter(s -> s.state() == ServiceState.PENDING).map(Service::serviceId).toList();
        }
    }

    private InMemoryServices repo;
    private List<ActivationJob> scheduled;
    private List<ServiceInstanceRecord> inventoried;
    private int provisionCalls;
    private ProvisioningResult networkResult;
    private ActivateServiceService useCase;

    @BeforeEach
    void setUp() {
        repo = new InMemoryServices();
        scheduled = new ArrayList<>();
        inventoried = new ArrayList<>();
        provisionCalls = 0;
        networkResult = ProvisioningResult.ok();
        NetworkProvisioningPort port = r -> { provisionCalls++; return networkResult; };
        ServiceInventory inventory = (record, token) -> inventoried.add(record);
        ActivationScheduler scheduler = scheduled::add; // exécution manuelle dans les tests
        useCase = new ActivateServiceService(repo, port, inventory, scheduler, new RecordingAuditTrail(), new FixedClock());
    }

    private Command command(UUID serviceId, String client) {
        return new Command(serviceId, UUID.randomUUID(), ClientId.of(client), "DRN-0231", new Imsi("302720123456789"),
                "C2_URLLC", List.of(new Characteristic("sst", "2")), Instant.parse("2026-10-15T18:58:01Z"), "jwt", "corr-1");
    }

    @Test
    void acceptStoresPendingAndSchedulesWithoutCallingTheNetwork() {
        var r = useCase.accept(command(UUID.randomUUID(), "client-a"));
        assertThat(r.created()).isTrue();
        assertThat(r.service().state()).isEqualTo(ServiceState.PENDING);
        assertThat(scheduled).hasSize(1);
        assertThat(provisionCalls).isZero();
    }

    @Test
    void successfulActivationBecomesActiveAndFeedsTheInventory() {
        var r = useCase.accept(command(UUID.randomUUID(), "client-a"));
        useCase.execute(scheduled.get(0));
        Service s = repo.store.get(r.service().serviceId());
        assertThat(s.state()).isEqualTo(ServiceState.ACTIVE);
        assertThat(s.activatedAt()).isNotNull();
        assertThat(inventoried).hasSize(1);
    }

    @Test
    void failedActivationBecomesFailedAndNeverTouchesTheInventory() {
        networkResult = ProvisioningResult.failed("MOCK_NETWORK_FAILURE");
        var r = useCase.accept(command(UUID.randomUUID(), "client-a"));
        useCase.execute(scheduled.get(0));
        Service s = repo.store.get(r.service().serviceId());
        assertThat(s.state()).isEqualTo(ServiceState.FAILED);
        assertThat(s.failureReason()).isEqualTo("MOCK_NETWORK_FAILURE");
        assertThat(inventoried).isEmpty();
    }

    @Test
    void replayWithSameServiceIdCreatesNothingNew() {
        UUID id = UUID.randomUUID();
        useCase.accept(command(id, "client-a"));
        var again = useCase.accept(command(id, "client-a"));
        assertThat(again.created()).isFalse();
        assertThat(scheduled).hasSize(1);
        assertThat(repo.store).hasSize(1);
    }

    @Test
    void secondConcurrentExecutionDoesNotProvisionTwice() {
        var r = useCase.accept(command(UUID.randomUUID(), "client-a"));
        ActivationJob job = scheduled.get(0);
        useCase.execute(job);
        useCase.execute(job);
        assertThat(provisionCalls).isEqualTo(1);
        assertThat(r.service().state()).isEqualTo(ServiceState.ACTIVE);
    }

    @Test
    void networkExceptionIsTreatedAsAFailure() {
        var failing = new ActivateServiceService(repo, r -> { throw new IllegalStateException("boom"); },
                (record, token) -> inventoried.add(record), scheduled::add, new RecordingAuditTrail(), new FixedClock());
        var r = failing.accept(command(UUID.randomUUID(), "client-a"));
        failing.execute(scheduled.get(0));
        assertThat(repo.store.get(r.service().serviceId()).state()).isEqualTo(ServiceState.FAILED);
    }

    @Test
    void inventoryFailureDoesNotUndoTheActivation() {
        var flaky = new ActivateServiceService(repo, r -> ProvisioningResult.ok(),
                (record, token) -> { throw new IllegalStateException("inventory down"); }, scheduled::add,
                new RecordingAuditTrail(), new FixedClock());
        var r = flaky.accept(command(UUID.randomUUID(), "client-a"));
        flaky.execute(scheduled.get(0));
        assertThat(repo.store.get(r.service().serviceId()).state()).isEqualTo(ServiceState.ACTIVE);
    }

    @Test
    void resumeReplaysPendingAndInterruptedActivations() {
        var a = useCase.accept(command(UUID.randomUUID(), "client-a"));
        var b = useCase.accept(command(UUID.randomUUID(), "client-a"));
        repo.claimForActivation(b.service().serviceId()); // b est resté ACTIVATING après un arrêt brutal
        scheduled.clear();
        int n = new ResumePendingActivationsService(repo, scheduled::add).resume();
        assertThat(n).isEqualTo(2);
        scheduled.forEach(useCase::execute);
        assertThat(repo.store.get(a.service().serviceId()).state()).isEqualTo(ServiceState.ACTIVE);
        assertThat(repo.store.get(b.service().serviceId()).state()).isEqualTo(ServiceState.ACTIVE);
    }

    @Test
    void servicesAreIsolatedByClient() {
        var r = useCase.accept(command(UUID.randomUUID(), "client-a"));
        assertThat(repo.findByOrder(ClientId.of("client-b"), r.service().orderId())).isEmpty();
    }
}
