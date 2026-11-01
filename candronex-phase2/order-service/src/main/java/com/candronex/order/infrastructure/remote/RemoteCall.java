package com.candronex.order.infrastructure.remote;

import com.candronex.order.domain.DomainException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

public class RemoteCall {

    private final String name;
    private final RestClient client;
    private final CircuitBreaker breaker;

    public RemoteCall(String name, RemoteProperties.Target target) {
        HttpClient http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(target.connectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(target.readTimeoutMs()));
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
                .slidingWindowSize(100)
                .minimumNumberOfCalls(50)
                .failureRateThreshold(50)
                .permittedNumberOfCallsInHalfOpenState(20)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .ignoreExceptions(HttpClientErrorException.class)
                .build());
    }

    public RestClient client() { return client; }

        public <T> T call(Supplier<T> supplier) {
        try {
            return CircuitBreaker.decorateSupplier(breaker, supplier).get();
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (CallNotPermittedException e) {
            throw DomainException.unavailable("Service " + name + " momentanément indisponible");
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof HttpTimeoutException) {
                throw DomainException.timeout("Délai dépassé en appelant " + name);
            }
            throw DomainException.unavailable("Service " + name + " indisponible");
        } catch (HttpServerErrorException e) {
            throw DomainException.unavailable("Service " + name + " en erreur");
        }
    }
}
