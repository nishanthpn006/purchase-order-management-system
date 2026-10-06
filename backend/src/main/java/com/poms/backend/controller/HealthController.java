package com.poms.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public health-check endpoint.
 * GET /api/health -> 200 {"status": "UP"}
 * No JWT token required.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Application liveness check")
public class HealthController {

    /**
     * GET /api/health
     * Returns HTTP 200 with {"status":"UP"} when the application is running.
     * This endpoint is intentionally public and does not require authentication.
     */
    @GetMapping("/health")
    @Operation(
            summary = "Application health check",
            description = "Public endpoint. Returns HTTP 200 with status UP when the application is running. Does not require a JWT token."
    )
    @SecurityRequirements // explicitly marks this endpoint as public in Swagger
    @ApiResponse(responseCode = "200", description = "Application is UP")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
