package com.candronex.slice.integration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** MOCK_FAILURE_RATE=100 : l'activation doit produire FAILED et aucune écriture dans l'inventaire. */
@SpringBootTest(properties = {"candronex.mock.latency-min-ms=0", "candronex.mock.latency-max-ms=0", "candronex.mock.failure-rate=100"})
class SliceFailureIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @MockBean ServiceInventory inventory;

    @Test
    void failureRate100ProducesFailed() throws Exception {
        UUID service = UUID.randomUUID(), order = UUID.randomUUID();
        mvc.perform(post("/api/v1/services/activation").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                .content(SliceApiIntegrationTest.request(service, order))).andExpect(status().isAccepted());
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                mvc.perform(get("/api/v1/services/activation").with(client("client-a")).param("orderId", order.toString()))
                        .andExpect(jsonPath("$[0].state").value("FAILED"))
                        .andExpect(jsonPath("$[0].failureReason").value("MOCK_NETWORK_FAILURE")));
        verify(inventory, never()).record(any(), any());
    }
}
