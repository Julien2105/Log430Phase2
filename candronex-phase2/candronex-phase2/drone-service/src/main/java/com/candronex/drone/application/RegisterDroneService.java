package com.candronex.drone.application;

import com.candronex.drone.domain.AuditTrail;
import com.candronex.drone.domain.Clock;
import com.candronex.drone.domain.DomainException;
import com.candronex.drone.domain.Drone;
import com.candronex.drone.domain.DroneRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RegisterDroneService implements RegisterDroneUseCase {

    private final DroneRepository drones;
    private final AuditTrail audit;
    private final Clock clock;

    public RegisterDroneService(DroneRepository drones, AuditTrail audit, Clock clock) {
        this.drones = drones;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public Result register(Command c) {
        Optional<Drone> existing = drones.find(c.clientId(), c.droneId());
        if (existing.isPresent()) {
            if (existing.get().sameContentAs(c.imsi(), c.label())) {
                return new Result(existing.get(), false); // rejeu : même contenu, même ressource
            }
            throw DomainException.conflict("DRONE_CONFLICT",
                    "Un drone avec cet identifiant existe déjà avec un contenu différent");
        }
        if (drones.imsiExists(c.imsi())) {
            throw DomainException.conflict("IMSI_ALREADY_REGISTERED", "Cet IMSI est déjà associé à un autre drone");
        }
        Drone saved = drones.save(new Drone(c.clientId(), c.droneId(), c.imsi(), c.label(), clock.now()));
        audit.record("DRONE_REGISTERED", c.clientId().value(), null, "droneId=" + c.droneId());
        return new Result(saved, true);
    }
}
