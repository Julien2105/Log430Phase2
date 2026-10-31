package com.candronex.order.api;

import com.candronex.order.application.CreateOrderUseCase;
import com.candronex.order.application.GetOrderQuery;
import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.IdempotencyKey;
import com.candronex.order.domain.OrderId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Commandes")
public class OrderController {

    private final CreateOrderUseCase create;
    private final GetOrderQuery query;

    public OrderController(CreateOrderUseCase create, GetOrderQuery query) {
        this.create = create;
        this.query = query;
    }

    @PostMapping
    @Operation(summary = "Créer une commande (UC-04)",
            description = "Idempotency-Key obligatoire. 201 à la création; 200 au rejeu (même contenu); 409 si le contenu diffère.")
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                @Valid @RequestBody CreateOrderRequest body) {
        var items = body.items().stream()
                .map(i -> new CreateOrderUseCase.ItemCommand(i.action(), i.droneId(), i.serviceOfferingId())).toList();
        var result = create.create(new CreateOrderUseCase.Command(Clients.of(jwt), new IdempotencyKey(idempotencyKey), items));
        OrderResponse response = OrderResponse.from(result.order());
        if (!result.created()) return ResponseEntity.ok(response);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + response.orderId())).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister les commandes du client authentifié")
    public List<OrderResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return query.list(Clients.of(jwt)).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Consulter une commande et l'état de ses services")
    public OrderResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable String orderId) {
        UUID id;
        try {
            id = UUID.fromString(orderId);
        } catch (IllegalArgumentException e) {
            throw DomainException.notFound("Commande introuvable");
        }
        return OrderResponse.from(query.get(Clients.of(jwt), OrderId.of(id)));
    }
}
