package com.candronex.slice.infrastructure;

import com.candronex.slice.domain.NetworkProvisioningPort;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Adaptateur simulé du réseau 5G (Phase 2) : latence aléatoire [min, max] et échecs selon un pourcentage.
 * MOCK_LATENCY_MIN_MS=300, MOCK_LATENCY_MAX_MS=1500, MOCK_FAILURE_RATE=0 (100 -> tout échoue -> FAILED).
 */
@Component
@EnableConfigurationProperties(MockProperties.class)
public class MockOpen5GSAdapter implements NetworkProvisioningPort {

    private static final Logger LOG = LoggerFactory.getLogger(MockOpen5GSAdapter.class);

    private final MockProperties props;
    private final Random random;

    @Autowired
    public MockOpen5GSAdapter(MockProperties props) {
        this(props, new Random());
    }

    public MockOpen5GSAdapter(MockProperties props, Random random) {
        this.props = props;
        this.random = random;
    }

    @Override
    public ProvisioningResult provision(ProvisioningRequest request) {
        long min = Math.max(0, props.latencyMinMs());
        long max = Math.max(min, props.latencyMaxMs());
        long latency = min + (long) (random.nextDouble() * (max - min + 1));
        LOG.info("event=NETWORK_PROVISIONING_REQUESTED serviceId={} latencyMs={}", request.serviceId(), latency);
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ProvisioningResult.failed("PROVISIONING_INTERRUPTED");
        }
        boolean fails = random.nextDouble() * 100.0 < props.failureRate();
        LOG.info("event=NETWORK_PROVISIONING_RESPONSE serviceId={} success={}", request.serviceId(), !fails);
        return fails ? ProvisioningResult.failed("MOCK_NETWORK_FAILURE") : ProvisioningResult.ok();
    }
}
