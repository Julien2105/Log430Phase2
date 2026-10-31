package com.candronex.drone.application;

import com.candronex.drone.domain.ClientId;
import com.candronex.drone.domain.Drone;
import com.candronex.drone.domain.DroneId;
import com.candronex.drone.domain.Imsi;

/** UC-02 : enregistrer un drone et son IMSI. */
public interface RegisterDroneUseCase {

    record Command(ClientId clientId, DroneId droneId, Imsi imsi, String label) { }

    record Result(Drone drone, boolean created) { }

    Result register(Command command);
}
