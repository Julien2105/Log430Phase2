package com.candronex.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.candronex.catalog.application.ConsultCatalogService;
import com.candronex.catalog.domain.DomainException;
import com.candronex.catalog.domain.ServiceCharacteristic;
import com.candronex.catalog.domain.ServiceOffering;
import com.candronex.catalog.domain.ServiceOfferingId;
import com.candronex.catalog.domain.ServiceOfferingRepository;
import com.candronex.catalog.domain.ServiceSpecification;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConsultCatalogServiceTest {

    private final ServiceOffering urllc = new ServiceOffering(ServiceOfferingId.of("C2_URLLC"), "Command and Control", "d",
            new ServiceSpecification("SPEC_URLLC", "URLLC", List.of(new ServiceCharacteristic("5qi", "82"))));

    private final ServiceOfferingRepository repo = new ServiceOfferingRepository() {
        @Override public List<ServiceOffering> findAll() { return List.of(urllc); }
        @Override public Optional<ServiceOffering> find(ServiceOfferingId id) {
            return id.equals(urllc.id()) ? Optional.of(urllc) : Optional.empty();
        }
    };

    private final ConsultCatalogService service = new ConsultCatalogService(repo);

    @Test
    void listsOfferingsWithTheirSpecification() {
        assertThat(service.list()).hasSize(1);
        assertThat(service.list().get(0).specification().characteristics()).extracting(ServiceCharacteristic::name).contains("5qi");
    }

    @Test
    void unknownOfferingIsNotFound() {
        assertThatThrownBy(() -> service.get(ServiceOfferingId.of("UNKNOWN")))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.kind()).isEqualTo(DomainException.Kind.NOT_FOUND));
    }
}
