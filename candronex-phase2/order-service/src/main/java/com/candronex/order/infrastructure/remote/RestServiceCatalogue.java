package com.candronex.order.infrastructure.remote;

import com.candronex.order.domain.Characteristic;
import com.candronex.order.domain.DomainException;
import com.candronex.order.domain.ServiceCatalogue;
import com.candronex.order.domain.ServiceOfferingId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class RestServiceCatalogue implements ServiceCatalogue {

    record CharacteristicDto(String name, String value) { }

    record OfferingDto(String serviceOfferingId, List<CharacteristicDto> serviceCharacteristics) { }

    private static final long TTL_NANOS = 60_000_000_000L;

    private record Cached(Optional<OfferingSnapshot> value, long expiresAt) { }

    private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

    private final RemoteCall remote;

    public RestServiceCatalogue(RemoteProperties props) { this.remote = new RemoteCall("catalog-service", props.catalog()); }

    @Override
    public Optional<OfferingSnapshot> find(ServiceOfferingId id) {
        long now = System.nanoTime();
        Cached hit = cache.get(id.value());
        if (hit != null && hit.expiresAt() > now) return hit.value();
        Optional<OfferingSnapshot> fresh = load(id);
        if (fresh.isPresent()) cache.put(id.value(), new Cached(fresh, now + TTL_NANOS));
        return fresh;
    }

    private Optional<OfferingSnapshot> load(ServiceOfferingId id) {
        try {
            OfferingDto dto = remote.call(() -> remote.client().get()
                    .uri("/api/v1/service-offerings/{id}", id.value())
                    .retrieve().body(OfferingDto.class));
            List<Characteristic> chars = dto.serviceCharacteristics().stream()
                    .map(c -> new Characteristic(c.name(), c.value())).toList();
            return Optional.of(new OfferingSnapshot(id, chars));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw DomainException.unavailable("Réponse inattendue de catalog-service");
        }
    }
}
