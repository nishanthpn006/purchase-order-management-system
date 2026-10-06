package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Safe user profile — password is excluded.
 * Used for GET /api/me response.
 */
@Schema(description = "Safe user profile representation without credentials")
public class UserResponse {

    @Schema(description = "Unique user identifier", example = "1")
    private Integer id;

    @Schema(description = "User full name", example = "Nishanth PN")
    private String fullName;

    @Schema(description = "User email address", example = "nishanth@poms.com")
    private String email;

    @Schema(description = "User role in the system (Admin, Manager, Employee)", example = "Admin")
    private String role;

    @Schema(description = "Account status (Active, Inactive)", example = "Active")
    private String status;

    public UserResponse() {
    }

    public UserResponse(Integer id, String fullName, String email, String role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
