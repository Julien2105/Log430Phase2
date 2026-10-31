package com.candronex.drone.api;

import com.candronex.drone.application.DroneQuery;
import com.candronex.drone.application.RegisterDroneUseCase;
import com.candronex.drone.domain.DroneId;
import com.candronex.drone.domain.Imsi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drones")
@Tag(name = "Drones")
public class DroneController {

    private final RegisterDroneUseCase register;
    private final DroneQuery query;

    public DroneController(RegisterDroneUseCase register, DroneQuery query) {
        this.register = register;
        this.query = query;
    }

    @PostMapping
    @Operation(summary = "Enregistrer un drone (UC-02)",
            description = "201 à la création, 200 si le même drone est rejoué avec le même contenu, 409 si le contenu diffère.")
    public ResponseEntity<DroneResponse> register(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RegisterDroneRequest body) {
        var result = register.register(new RegisterDroneUseCase.Command(
                Clients.of(jwt), DroneId.of(body.droneId()), Imsi.of(body.imsi()), body.label()));
        DroneResponse response = DroneResponse.from(result.drone());
        if (!result.created()) return ResponseEntity.ok(response);
        return ResponseEntity.created(URI.create("/api/v1/drones/" + response.droneId())).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister les drones du client authentifié")
    public List<DroneResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return query.list(Clients.of(jwt)).stream().map(DroneResponse::from).toList();
    }

    @GetMapping("/{droneId}")
    @Operation(summary = "Consulter un drone du client authentifié")
    public DroneResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable String droneId) {
        return DroneResponse.from(query.get(Clients.of(jwt), DroneId.of(droneId)));
    }

    @GetMapping("/{droneId}/network-identity")
    @Operation(summary = "Identité réseau d'un drone (interne, non publiée par la passerelle)")
    public NetworkIdentityResponse networkIdentity(@AuthenticationPrincipal Jwt jwt, @PathVariable String droneId) {
        return NetworkIdentityResponse.from(query.get(Clients.of(jwt), DroneId.of(droneId)));
    }
}
