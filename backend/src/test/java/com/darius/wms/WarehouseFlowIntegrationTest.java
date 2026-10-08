package com.darius.wms;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Full HTTP -> controller -> service -> Hibernate -> database round trips,
 * against an in-memory H2 database migrated by the same Flyway scripts as production.
 * No test-level transaction: every request commits or rolls back exactly like in production,
 * so each test uses its own SKUs and location codes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN") // security itself is covered by SecurityIntegrationTest
class WarehouseFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void receivePickAndShipAnOrder() throws Exception {
        long productId = createProduct("TEST-001", 5);
        long locationA = createLocation("T", 1, 1, 100);
        long locationB = createLocation("T", 1, 2, 100);

        receive(productId, locationA, 6).andExpect(status().isOk());
        receive(productId, locationB, 4).andExpect(jsonPath("$.quantity").value(4));

        long orderId = createOrder(productId, 8);

        // 8 units must come from two locations (6 + 2)
        mvc.perform(post("/api/orders/{id}/pick", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED"));

        mvc.perform(get("/api/products/{id}", productId))
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.lowStock").value(true));

        mvc.perform(post("/api/orders/{id}/ship", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void pickingFailsWithoutEnoughStockAndChangesNothing() throws Exception {
        long productId = createProduct("TEST-002", 0);
        long location = createLocation("T", 2, 1, 100);
        receive(productId, location, 3);

        long orderId = createOrder(productId, 5);

        mvc.perform(post("/api/orders/{id}/pick", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("requested 5, available 3")));

        mvc.perform(get("/api/orders/{id}", orderId)).andExpect(jsonPath("$.status").value("NEW"));
        mvc.perform(get("/api/products/{id}", productId)).andExpect(jsonPath("$.totalQuantity").value(3));
    }

    @Test
    void cannotOverfillALocation() throws Exception {
        long productId = createProduct("TEST-003", 0);
        long location = createLocation("T", 3, 1, 10);

        receive(productId, location, 8).andExpect(status().isOk());
        receive(productId, location, 5)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("room for only 2")));
    }

    @Test
    void transferMovesStockBetweenLocations() throws Exception {
        long productId = createProduct("TEST-004", 0);
        long from = createLocation("T", 4, 1, 100);
        long to = createLocation("T", 4, 2, 100);
        receive(productId, from, 10);

        mvc.perform(post("/api/stock/transfer").contentType(MediaType.APPLICATION_JSON).content("""
                        {"productId": %d, "fromLocationId": %d, "toLocationId": %d, "quantity": 4}
                        """.formatted(productId, from, to)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationCode").value("T-04-02"))
                .andExpect(jsonPath("$.quantity").value(4));

        mvc.perform(get("/api/locations/{id}", from)).andExpect(jsonPath("$.usedCapacity").value(6));
    }

    @Test
    void invalidProductIsRejectedWithFieldErrors() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
                        {"sku": "bad sku!", "name": "", "unit": "PIECE", "minStockLevel": -1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.minStockLevel").exists());
    }

    @Test
    void dashboardReturnsSeedData() throws Exception {
        mvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.zones.length()").value(4));
    }

    // ---- helpers ----------------------------------------------------------

    private long createProduct(String sku, int minStock) throws Exception {
        return idOf(mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
                {"sku": "%s", "name": "Test product", "unit": "PIECE", "minStockLevel": %d}
                """.formatted(sku, minStock))).andExpect(status().isCreated()));
    }

    private long createLocation(String aisle, int rack, int shelf, int capacity) throws Exception {
        return idOf(mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON).content("""
                {"zone": "STORAGE", "aisle": "%s", "rack": %d, "shelf": %d, "capacity": %d}
                """.formatted(aisle, rack, shelf, capacity))).andExpect(status().isCreated()));
    }

    private ResultActions receive(long productId, long locationId, int quantity) throws Exception {
        return mvc.perform(post("/api/stock/receive").contentType(MediaType.APPLICATION_JSON).content("""
                {"productId": %d, "locationId": %d, "quantity": %d, "reference": "PO-TEST"}
                """.formatted(productId, locationId, quantity)));
    }

    private long createOrder(long productId, int quantity) throws Exception {
        return idOf(mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"customerName": "Test customer", "lines": [{"productId": %d, "quantity": %d}]}
                """.formatted(productId, quantity))).andExpect(status().isCreated()));
    }

    private static long idOf(ResultActions result) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
