package com.darius.wms.security;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Real login + real JWTs (no mocked users): checks authentication and the role rules end to end. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void loginReturnsTokenThatGrantsAccess() throws Exception {
        String token = login("admin", "admin123");

        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles", contains("ADMIN")));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void apiRequiresAToken() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = login("admin", "admin123");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        mvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorCanReadAndWorkStockButNotEditMasterData() throws Exception {
        String bearer = "Bearer " + login("operator", "operator123");

        mvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk());

        mvc.perform(post("/api/products").header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SEC-1\",\"name\":\"x\",\"unit\":\"PIECE\",\"minStockLevel\":0}"))
                .andExpect(status().isForbidden());

        // Receive 1 unit of a seeded product (id 1) into a seeded location (id 1) and check the audit trail
        mvc.perform(post("/api/stock/receive").header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"locationId\":1,\"quantity\":1,\"reference\":\"SEC-TEST\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/stock/movements?productId=1&size=1").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(jsonPath("$.content[0].reference").value("SEC-TEST"))
                .andExpect(jsonPath("$.content[0].performedBy").value("operator"));
    }

    @Test
    void swaggerIsPublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
