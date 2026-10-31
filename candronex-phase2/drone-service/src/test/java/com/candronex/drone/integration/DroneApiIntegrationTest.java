package com.candronex.drone.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** I-01 à I-06 : API REST de DroneService avec PostgreSQL réel. */
class DroneApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;

    private static String body(String id, String imsi) {
        return "{\"droneId\":\"" + id + "\",\"imsi\":\"" + imsi + "\",\"label\":\"Inspection\"}";
    }

    @Test
    void withoutTokenReturns401ProblemDetails() throws Exception {
        mvc.perform(get("/api/v1/drones"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createReturns201AndNeverExposesTheImsi() throws Exception {
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                        .content(body("DRN-0231", "302720000000001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.droneId").value("DRN-0231"))
                .andExpect(jsonPath("$.imsi").doesNotExist());
    }

    @Test
    void replayWithSameContentReturns200() throws Exception {
        String json = body("DRN-0300", "302720000000002");
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    void otherClientCannotSeeTheDrone() throws Exception {
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                .content(body("DRN-0400", "302720000000003"))).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/drones/DRN-0400").with(client("client-b")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void sameDroneIdCanExistForTwoClients() throws Exception {
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                .content(body("DRN-0500", "302720000000004"))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/drones").with(client("client-b")).contentType(MediaType.APPLICATION_JSON)
                .content(body("DRN-0500", "302720000000005"))).andExpect(status().isCreated());
    }

    @Test
    void invalidImsiReturns400WithCorrelationId() throws Exception {
        mvc.perform(post("/api/v1/drones").with(client("client-a")).header("X-Correlation-Id", "corr-123")
                        .contentType(MediaType.APPLICATION_JSON).content(body("DRN-0600", "12")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_IMSI"))
                .andExpect(jsonPath("$.correlationId").value("corr-123"));
    }

    @Test
    void imsiAlreadyRegisteredReturns409() throws Exception {
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                .content(body("DRN-0700", "302720000000006"))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/drones").with(client("client-a")).contentType(MediaType.APPLICATION_JSON)
                        .content(body("DRN-0701", "302720000000006")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IMSI_ALREADY_REGISTERED"));
    }
}
