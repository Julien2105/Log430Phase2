package com.candronex.order.infrastructure.remote;

import com.candronex.order.domain.DomainException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Appel REST sortant : délais d'attente, disjoncteur Resilience4j, propagation du JWT du client et de X-Correlation-Id,
 * traduction des pannes en 503 SERVICE_UNAVAILABLE / 504 UPSTREAM_TIMEOUT.
 */
public class RemoteCall {

    private final String name;
    private final RestClient client;
    private final CircuitBreaker breaker;

    public RemoteCall(String name, RemoteProperties.Target target) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(target.connectTimeoutMs());
        factory.setReadTimeout(target.readTimeoutMs());
        this.name = name;
        this.client = RestClient.builder().baseUrl(target.baseUrl()).requestFactory(factory)
                .requestInterceptor((request, body, execution) -> {
                    Authentication a = SecurityContextHolder.getContext().getAuthentication();
                    if (a != null && a.getPrincipal() instanceof Jwt jwt) request.getHeaders().setBearerAuth(jwt.getTokenValue());
                    String correlationId = MDC.get("correlationId");
                    if (correlationId != null) request.getHeaders().set("X-Correlation-Id", correlationId);
                    return execution.execute(request, body);
                }).build();
        this.breaker = CircuitBreaker.of(name, CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .ignoreExceptions(HttpClientErrorException.class)
                .build());
    }

    public RestClient client() { return client; }

    /** Exécute l'appel; les erreurs 4xx remontent telles quelles (à traiter par l'appelant). */
    public <T> T call(Supplier<T> supplier) {
        try {
            return CircuitBreaker.decorateSupplier(breaker, supplier).get();
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (CallNotPermittedException e) {
            throw DomainException.unavailable("Service " + name + " momentanément indisponible");
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw DomainException.timeout("Délai dépassé en appelant " + name);
            }
            throw DomainException.unavailable("Service " + name + " indisponible");
        } catch (HttpServerErrorException e) {
            throw DomainException.unavailable("Service " + name + " en erreur");
        }
    }
}
