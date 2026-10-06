package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for POST /api/login
 */
@Schema(description = "Login credentials request payload")
public class LoginRequest {

    @Schema(description = "User email address", example = "nishanth@poms.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Account password", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
