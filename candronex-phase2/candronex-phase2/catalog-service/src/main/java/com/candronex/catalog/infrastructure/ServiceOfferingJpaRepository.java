package com.candronex.catalog.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOfferingJpaEntity, String> {

    List<ServiceOfferingJpaEntity> findAllByOrderById();
}
