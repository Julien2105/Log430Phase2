package com.candronex.catalog.api;

import com.candronex.catalog.domain.ClientId;
import org.springframework.security.oauth2.jwt.Jwt;

/** Extrait l'identité du client B2B du JWT (claim clientId) : jamais d'en-tête X-Client-Id. */
public final class Clients {

    private Clients() { }

    public static ClientId of(Jwt jwt) {
        return ClientId.of(jwt.getClaimAsString("clientId"));
    }
}
