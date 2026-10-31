package com.candronex.inventory.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class InventoryApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;

    private static String body(String clientId) {
        return "{\"clientId\":\"" + clientId + "\",\"orderId\":\"" + UUID.randomUUID()
                + "\",\"droneId\":\"DRN-0231\",\"serviceOfferingId\":\"C2_URLLC\",\"state\":\"ACTIVE\",\"activatedAt\":\"2026-10-15T18:58:02Z\"}";
    }

    @Test
    void putThenGetReturnsTheActiveServiceWithoutImsi() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(put("/api/v1/services/" + id).with(client("client-a")).contentType(MediaType.APPLICATION_JSON).content(body("client-a")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/services/" + id).with(client("client-a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.imsi").doesNotExist());
    }

    @Test
    void anotherClientGets404() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(put("/api/v1/services/" + id).with(client("client-a")).contentType(MediaType.APPLICATION_JSON).content(body("client-a")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/services/" + id).with(client("client-b"))).andExpect(status().isNotFound());
    }

    @Test
    void aClientCannotWriteOnBehalfOfAnother() throws Exception {
        mvc.perform(put("/api/v1/services/" + UUID.randomUUID()).with(client("client-b")).contentType(MediaType.APPLICATION_JSON)
                        .content(body("client-a")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void trustedServiceClientMayWriteForAClient() throws Exception {
        mvc.perform(put("/api/v1/services/" + UUID.randomUUID()).with(client("slice-service")).contentType(MediaType.APPLICATION_JSON)
                        .content(body("client-a")))
                .andExpect(status().isOk());
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/v1/services")).andExpect(status().isUnauthorized());
    }
}
