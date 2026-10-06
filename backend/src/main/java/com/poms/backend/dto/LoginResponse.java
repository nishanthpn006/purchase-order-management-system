package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body for a successful login — returns the JWT token.
 */
@Schema(description = "Authentication response containing JWT token")
public class LoginResponse {

    @Schema(description = "JWT Bearer access token valid for 24 hours", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
