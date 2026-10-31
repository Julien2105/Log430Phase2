package com.candronex.slice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.candronex.slice.domain.Characteristic;
import com.candronex.slice.domain.ClientId;
import com.candronex.slice.domain.DomainException;
import com.candronex.slice.domain.Imsi;
import com.candronex.slice.domain.Service;
import com.candronex.slice.domain.ServiceState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Règles du domaine : états PENDING, ACTIVATING, ACTIVE, FAILED. */
class ServiceTest {

    private Service newService() {
        return Service.accept(UUID.randomUUID(), UUID.randomUUID(), ClientId.of("client-a"), "DRN-0231",
                new Imsi("302720123456789"), "C2_URLLC", List.of(new Characteristic("sst", "2")), Instant.parse("2026-10-15T18:58:01Z"));
    }

    @Test
    void startsPending() { assertThat(newService().state()).isEqualTo(ServiceState.PENDING); }

    @Test
    void pendingToActivatingToActiveSetsActivatedAt() {
        Service s = newService();
        s.startActivation();
        s.markActive(Instant.parse("2026-10-15T18:58:03Z"));
        assertThat(s.state()).isEqualTo(ServiceState.ACTIVE);
        assertThat(s.activatedAt()).isEqualTo(Instant.parse("2026-10-15T18:58:03Z"));
    }

    @Test
    void failureKeepsTheReasonAndNoActivationDate() {
        Service s = newService();
        s.startActivation();
        s.markFailed("MOCK_NETWORK_FAILURE");
        assertThat(s.state()).isEqualTo(ServiceState.FAILED);
        assertThat(s.activatedAt()).isNull();
        assertThat(s.failureReason()).isEqualTo("MOCK_NETWORK_FAILURE");
    }

    @Test
    void cannotActivateWithoutStartingFirst() {
        assertThatThrownBy(() -> newService().markActive(Instant.now())).isInstanceOf(DomainException.class);
    }

    @Test
    void terminalStatesCannotRestart() {
        Service s = newService();
        s.startActivation();
        s.markFailed("x");
        assertThatThrownBy(s::startActivation).isInstanceOf(DomainException.class);
    }

    @Test
    void interruptedActivationResumesToPending() {
        Service s = newService();
        s.startActivation();
        s.resume();
        assertThat(s.state()).isEqualTo(ServiceState.PENDING);
    }
}
