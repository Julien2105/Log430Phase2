package com.candronex.catalog.integration;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.testcontainers.containers.PostgreSQLContainer;

/** Base des tests d'intégration : PostgreSQL réel (Testcontainers) + MockMvc avec JWT simulé (claim clientId). */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
public abstract class AbstractIntegrationTest {

    // Conteneur singleton partagé par toutes les classes de test du service (démarré une seule fois).
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    protected static RequestPostProcessor client(String clientId) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.claim("clientId", clientId).audience(java.util.List.of("candronex-api")));
    }
}
