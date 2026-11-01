package com.candronex.slice.infrastructure;

import com.candronex.slice.domain.ServiceInventory;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(InventoryProperties.class)
public class RestServiceInventoryAdapter implements ServiceInventory {

    private final RestClient client;
    private final ServiceTokenProvider tokens;

    public RestServiceInventoryAdapter(InventoryProperties props, ServiceTokenProvider tokens) {
        HttpClient http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(props.connectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(props.readTimeoutMs()));
        this.client = RestClient.builder().baseUrl(props.baseUrl()).requestFactory(factory).build();
        this.tokens = tokens;
    }

    @Override
    public void record(ServiceInstanceRecord r, String bearerToken) {
        String token = bearerToken != null ? bearerToken : tokens.token();
        String correlationId = MDC.get("correlationId");
        client.put().uri("/api/v1/services/{id}", r.serviceId())
                .headers(h -> {
                    h.setBearerAuth(token);
                    if (correlationId != null) h.set("X-Correlation-Id", correlationId);
                })
                .body(Map.of(
                        "clientId", r.clientId().value(),
                        "orderId", r.orderId().toString(),
                        "droneId", r.droneId(),
                        "serviceOfferingId", r.serviceOfferingId(),
                        "state", "ACTIVE",
                        "activatedAt", r.activatedAt().toString()))
                .retrieve().toBodilessEntity();
    }
}
