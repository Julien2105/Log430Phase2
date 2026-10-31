package com.candronex.order.infrastructure.remote;

import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.DroneDirectory;
import com.candronex.order.domain.DroneId;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/** Adaptateur REST : GET /api/v1/drones/{droneId}/network-identity (API de DroneService, jamais son dépôt). */
@Component
public class RestDroneDirectory implements DroneDirectory {

    record IdentityDto(String droneId, String imsi) { }

    private final RemoteCall remote;

    public RestDroneDirectory(RemoteProperties props) { this.remote = new RemoteCall("drone-service", props.drone()); }

    @Override
    public NetworkIdentity networkIdentity(DroneId droneId) {
        try {
            IdentityDto dto = remote.call(() -> remote.client().get()
                    .uri("/api/v1/drones/{id}/network-identity", droneId.value())
                    .retrieve().body(IdentityDto.class));
            return new NetworkIdentity(droneId, dto.imsi());
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.Forbidden e) {
            throw DomainException.notFound("Drone introuvable"); // inexistant ou d'un autre client : même réponse
        } catch (HttpClientErrorException e) {
            throw DomainException.unavailable("Réponse inattendue de drone-service");
        }
    }
}
