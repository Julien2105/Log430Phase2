package com.candronex.slice.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Jeton technique (OAuth2 Client Credentials) utilisé uniquement pour la reprise, sans jeton client disponible. */
@Component
public class ServiceTokenProvider {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") long expiresIn) { }

    private final InventoryProperties props;
    private final RestClient client = RestClient.create();
    private String token;
    private Instant expiresAt = Instant.EPOCH;

    public ServiceTokenProvider(InventoryProperties props) { this.props = props; }

    public synchronized String token() {
        if (token == null || Instant.now().isAfter(expiresAt.minusSeconds(30))) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", props.clientId());
            form.add("client_secret", props.clientSecret());
            TokenResponse r = client.post().uri(props.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                    .retrieve().body(TokenResponse.class);
            token = r.accessToken();
            expiresAt = Instant.now().plusSeconds(r.expiresIn());
        }
        return token;
    }
}
