package com.poms.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit test for HealthController.
 *
 * HealthController has no dependencies, so no Spring context is needed.
 * This keeps the test fast and avoids the need for @SpringBootTest.
 *
 * Verifies:
 *   - GET /api/health returns HTTP 200
 *   - Response body contains {"status": "UP"}
 *   - Endpoint has no authentication dependencies (tested by the lack of any security mocking)
 */
class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    @DisplayName("health() returns HTTP 200 with status UP")
    void health_returnsOkWithStatusUp() {
        ResponseEntity<Map<String, String>> response = healthController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200");
        assertNotNull(response.getBody(), "Response body must not be null");
        assertEquals("UP", response.getBody().get("status"), "Expected status=UP in response body");
    }

    @Test
    @DisplayName("health() response body contains exactly one key: status")
    void health_responseBodyHasSingleStatusKey() {
        ResponseEntity<Map<String, String>> response = healthController.health();

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size(), "Response body should contain exactly one field");
        assertTrue(response.getBody().containsKey("status"), "Response body must contain 'status' key");
    }
}
