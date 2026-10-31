package com.candronex.slice;

import static org.assertj.core.api.Assertions.assertThat;

import com.candronex.slice.domain.NetworkProvisioningPort.ProvisioningRequest;
import com.candronex.slice.infrastructure.MockOpen5GSAdapter;
import com.candronex.slice.infrastructure.MockProperties;
import com.candronex.slice.domain.Imsi;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Tests du mock réseau : MOCK_FAILURE_RATE=100 produit FAILED, 0 produit un succès, la latence respecte [min, max]. */
class MockOpen5GSAdapterTest {

    private final ProvisioningRequest request = new ProvisioningRequest(UUID.randomUUID(), new Imsi("302720123456789"), List.of());

    @Test
    void failureRate100AlwaysFails() {
        var adapter = new MockOpen5GSAdapter(new MockProperties(0, 0, 100), new Random(1));
        for (int i = 0; i < 20; i++) assertThat(adapter.provision(request).success()).isFalse();
    }

    @Test
    void failureRate0NeverFails() {
        var adapter = new MockOpen5GSAdapter(new MockProperties(0, 0, 0), new Random(1));
        for (int i = 0; i < 20; i++) assertThat(adapter.provision(request).success()).isTrue();
    }

    @Test
    void latencyStaysWithinTheConfiguredBounds() {
        var adapter = new MockOpen5GSAdapter(new MockProperties(50, 80, 0), new Random(1));
        long start = System.nanoTime();
        adapter.provision(request);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertThat(elapsedMs).isGreaterThanOrEqualTo(45).isLessThan(500);
    }
}
