package com.candronex.drone.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroneJpaRepository extends JpaRepository<DroneJpaEntity, DroneKey> {

    List<DroneJpaEntity> findByClientIdOrderByDroneId(String clientId);

    boolean existsByImsi(String imsi);
}
