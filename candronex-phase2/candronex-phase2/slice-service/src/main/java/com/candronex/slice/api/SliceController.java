package com.candronex.slice.api;

import com.candronex.slice.application.ActivateServiceUseCase;
import com.candronex.slice.application.ConsultActivationQuery;
import com.candronex.slice.domain.Characteristic;
import com.candronex.slice.domain.Imsi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Routes internes (non déclarées dans KrakenD) : appelées uniquement par order-service. */
@RestController
@RequestMapping("/api/v1/services/activation")
@Tag(name = "Activation des services (interne)")
public class SliceController {

    private final ActivateServiceUseCase activate;
    private final ConsultActivationQuery query;

    public SliceController(ActivateServiceUseCase activate, ConsultActivationQuery query) {
        this.activate = activate;
        this.query = query;
    }

    @PostMapping
    @Operation(summary = "Demander l'activation d'un service (UC-06)",
            description = "202 Accepted : l'activation réseau est asynchrone. Idempotent par serviceId.")
    public ResponseEntity<ServiceStatusResponse> activate(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody ServiceActivationRequest body) {
        List<Characteristic> characteristics = body.serviceCharacteristic().stream()
                .map(c -> new Characteristic(c.name(), c.value())).toList();
        var result = activate.accept(new ActivateServiceUseCase.Command(body.serviceId(), body.orderId(), Clients.of(jwt),
                body.droneId(), new Imsi(body.imsi()), body.serviceOfferingId(), characteristics, body.acceptedAt(),
                jwt.getTokenValue(), MDC.get("correlationId")));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ServiceStatusResponse.from(result.service()));
    }

    @GetMapping
    @Operation(summary = "États des services d'une commande (interne)")
    public List<ServiceStatusResponse> byOrder(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID orderId) {
        return query.byOrder(Clients.of(jwt), orderId).stream().map(ServiceStatusResponse::from).toList();
    }
}
