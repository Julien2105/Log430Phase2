package com.candronex.catalog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class CatalogApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void requiresAToken() throws Exception {
        mvc.perform(get("/api/v1/service-offerings")).andExpect(status().isUnauthorized());
    }

    @Test
    void listsTheTwoOfferingsWithNetworkCharacteristics() throws Exception {
        mvc.perform(get("/api/v1/service-offerings").with(client("client-a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.serviceOfferingId=='C2_URLLC')].serviceCharacteristics[?(@.name=='5qi')].value").value("82"));
    }

    @Test
    void unknownOfferingReturns404() throws Exception {
        mvc.perform(get("/api/v1/service-offerings/NOPE").with(client("client-a")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
