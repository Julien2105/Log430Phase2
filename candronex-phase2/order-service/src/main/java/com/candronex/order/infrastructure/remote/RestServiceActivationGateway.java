package com.candronex.order.infrastructure.remote;

import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.LineId;
import com.candronex.order.domain.OrderId;
import com.candronex.order.domain.ServiceActivationGateway;
import com.candronex.order.domain.ServiceState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/** Adaptateur REST vers SliceService : POST /api/v1/services/activation et GET /api/v1/services/activation?orderId=. */
@Component
public class RestServiceActivationGateway implements ServiceActivationGateway {

    record StatusDto(String serviceId, String state) { }

    private final RemoteCall remote;

    public RestServiceActivationGateway(RemoteProperties props) { this.remote = new RemoteCall("slice-service", props.slice()); }

    @Override
    public void requestActivation(ActivationRequest r) {
        Map<String, Object> body = Map.of(
                "serviceId", r.serviceId().toString(),
                "orderId", r.orderId().toString(),
                "droneId", r.droneId().value(),
                "imsi", r.imsi(),
                "serviceOfferingId", r.serviceOfferingId().value(),
                "serviceCharacteristic", r.characteristics().stream()
                        .map(c -> Map.of("name", c.name(), "value", c.value())).toList(),
                "acceptedAt", r.acceptedAt().toString());
        try {
            remote.call(() -> remote.client().post().uri("/api/v1/services/activation")
                    .header("Idempotency-Key", r.serviceId().toString()) // clé = identifiant de la ligne
                    .body(body).retrieve().toBodilessEntity());
        } catch (HttpClientErrorException e) {
            throw DomainException.unavailable("Demande d'activation refusée par slice-service");
        }
    }

    @Override
    public Map<LineId, ServiceState> statesOf(OrderId orderId) {
        try {
            List<StatusDto> list = remote.call(() -> remote.client().get()
                    .uri("/api/v1/services/activation?orderId={id}", orderId.value())
                    .retrieve().body(new ParameterizedTypeReference<List<StatusDto>>() { }));
            Map<LineId, ServiceState> states = new HashMap<>();
            if (list != null) {
                list.forEach(s -> states.put(LineId.of(UUID.fromString(s.serviceId())), ServiceState.valueOf(s.state())));
            }
            return states;
        } catch (HttpClientErrorException e) {
            throw DomainException.unavailable("Lecture des états refusée par slice-service");
        }
    }
}
