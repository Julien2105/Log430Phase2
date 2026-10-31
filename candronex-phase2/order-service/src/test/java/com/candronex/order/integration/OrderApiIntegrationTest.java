package com.candronex.order.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.candronex.order.domain.Characteristic;
import com.candronex.order.domain.DroneDirectory;
import com.candronex.order.domain.DroneDirectory.NetworkIdentity;
import com.candronex.order.domain.ServiceActivationGateway;
import com.candronex.order.domain.ServiceCatalogue;
import com.candronex.order.domain.ServiceCatalogue.OfferingSnapshot;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Les trois services distants sont simulés au niveau des ports; PostgreSQL et la sécurité sont réels. */
class OrderApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @MockBean DroneDirectory drones;
    @MockBean ServiceCatalogue catalogue;
    @MockBean ServiceActivationGateway activation;

    private static final String ORDER = "{\"items\":[{\"action\":\"ADD\",\"droneId\":\"DRN-0231\",\"serviceOfferingId\":\"C2_URLLC\"}]}";
    private static final String OTHER = "{\"items\":[{\"action\":\"ADD\",\"droneId\":\"DRN-0231\",\"serviceOfferingId\":\"IMAGERY_EMBB\"}]}";

    @BeforeEach
    void stubs() {
        when(drones.networkIdentity(any())).thenAnswer(i -> new NetworkIdentity(i.getArgument(0), "302720123456789"));
        when(catalogue.find(any())).thenAnswer(i -> Optional.of(new OfferingSnapshot(i.getArgument(0),
                List.of(new Characteristic("sst", "2"), new Characteristic("5qi", "82")))));
        when(activation.statesOf(any())).thenReturn(Map.of());
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mvc.perform(post("/api/v1/orders").header("Idempotency-Key", "x").contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingIdempotencyKeyReturns400() throws Exception {
        mvc.perform(post("/api/v1/orders").with(client("client-a")).contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createReturns201WithSnapshotAndNoImsi() throws Exception {
        mvc.perform(post("/api/v1/orders").with(client("client-a")).header("Idempotency-Key", "it-1")
                        .contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.state").value("ACCEPTED"))
                .andExpect(jsonPath("$.items[0].service.serviceOfferingId").value("C2_URLLC"))
                .andExpect(jsonPath("$.items[0].service.serviceCharacteristic[?(@.name=='5qi')].value").value("82"))
                .andExpect(jsonPath("$..imsi").doesNotExist());
    }

    @Test
    void replayReturns200AndDifferentContentReturns409() throws Exception {
        mvc.perform(post("/api/v1/orders").with(client("client-a")).header("Idempotency-Key", "it-2")
                .contentType(MediaType.APPLICATION_JSON).content(ORDER)).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/orders").with(client("client-a")).header("Idempotency-Key", "it-2")
                .contentType(MediaType.APPLICATION_JSON).content(ORDER)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/orders").with(client("client-a")).header("Idempotency-Key", "it-2")
                        .contentType(MediaType.APPLICATION_JSON).content(OTHER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));
    }

    @Test
    void anotherClientCannotReadTheOrder() throws Exception {
        String location = mvc.perform(post("/api/v1/orders").with(client("client-a")).header("Idempotency-Key", "it-3")
                        .contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(location).with(client("client-b")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void sameKeyIsScopedPerClient() throws Exception {
        mvc.perform(post("/api/v1/orders").with(client("client-a")).header("Idempotency-Key", "shared")
                .contentType(MediaType.APPLICATION_JSON).content(ORDER)).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/orders").with(client("client-b")).header("Idempotency-Key", "shared")
                .contentType(MediaType.APPLICATION_JSON).content(ORDER)).andExpect(status().isCreated());
    }
}
