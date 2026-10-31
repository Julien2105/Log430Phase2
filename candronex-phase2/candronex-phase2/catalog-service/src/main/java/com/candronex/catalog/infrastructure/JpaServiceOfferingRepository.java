package com.candronex.catalog.infrastructure;

import com.candronex.catalog.domain.ServiceCharacteristic;
import com.candronex.catalog.domain.ServiceOffering;
import com.candronex.catalog.domain.ServiceOfferingId;
import com.candronex.catalog.domain.ServiceOfferingRepository;
import com.candronex.catalog.domain.ServiceSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur JPA du port ServiceOfferingRepository (base catalog_db). */
@Repository
public class JpaServiceOfferingRepository implements ServiceOfferingRepository {

    private final ServiceOfferingJpaRepository jpa;

    public JpaServiceOfferingRepository(ServiceOfferingJpaRepository jpa) { this.jpa = jpa; }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOffering> findAll() {
        return jpa.findAllByOrderById().stream().map(JpaServiceOfferingRepository::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceOffering> find(ServiceOfferingId id) {
        return jpa.findById(id.value()).map(JpaServiceOfferingRepository::toDomain);
    }

    private static ServiceOffering toDomain(ServiceOfferingJpaEntity e) {
        ServiceSpecificationJpaEntity s = e.getSpecification();
        List<ServiceCharacteristic> chars = s.getCharacteristics().stream()
                .map(c -> new ServiceCharacteristic(c.getName(), c.getValue())).toList();
        return new ServiceOffering(ServiceOfferingId.of(e.getId()), e.getName(), e.getDescription(),
                new ServiceSpecification(s.getId(), s.getName(), chars));
    }
}
