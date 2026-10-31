package com.candronex.slice.integration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.candronex.slice.domain.ServiceInventory;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Mock sans latence et sans échec : l'activation aboutit à ACTIVE et alimente l'inventaire. */
@SpringBootTest(properties = {"candronex.mock.latency-min-ms=0", "candronex.mock.latency-max-ms=0", "candronex.mock.failure-rate=0"})
class SliceApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @MockBean ServiceInventory inventory;

    static String request(UUID serviceId, UUID orderId) {
        return "{\"serviceId\":\"" + serviceId + "\",\"orderId\":\"" + orderId + "\",\"droneId\":\"DRN-0231\","
                + "\"imsi\":\"302720123456789\",\"serviceOfferingId\":\"C2_URLLC\",\"acceptedAt\":\"2026-10-15T18:58:01Z\","
                + "\"serviceCharacteristic\":[{\"name\":\"sliceType\",\"value\":\"URLLC\"},{\"name\":\"sst\",\"value\":\"2\"},"
                + "{\"name\":\"sd\",\"value\":\"000123\"},{\"name\":\"dnn\",\"value\":\"telemetry.candronex.com\"},"
                + "{\"name\":\"5qi\",\"value\":\"82\"}]}";
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/v1/services/activation").param("orderId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void activationIsAcceptedThenBecomesActiveAndNeverExposesTheImsi() throws Exception {
        UUID service = UUID.randomUUID(), order = UUID.randomUUID();
        mvc.perform(post("/api/v1/services/activation").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                        .content(request(service, order)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.imsi").doesNotExist());
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                mvc.perform(get("/api/v1/services/activation").with(client("client-a")).param("orderId", order.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].state").value("ACTIVE"))
                        .andExpect(jsonPath("$[0].activatedAt").exists()));
        verify(inventory, timeout(2000)).record(any(), any());
    }

    @Test
    void replayDoesNotCreateAnotherService() throws Exception {
        UUID service = UUID.randomUUID(), order = UUID.randomUUID();
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/v1/services/activation").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                    .content(request(service, order))).andExpect(status().isAccepted());
        }
        mvc.perform(get("/api/v1/services/activation").with(client("client-a")).param("orderId", order.toString()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void anotherClientSeesNothing() throws Exception {
        UUID service = UUID.randomUUID(), order = UUID.randomUUID();
        mvc.perform(post("/api/v1/services/activation").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                .content(request(service, order))).andExpect(status().isAccepted());
        mvc.perform(get("/api/v1/services/activation").with(client("client-b")).param("orderId", order.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
