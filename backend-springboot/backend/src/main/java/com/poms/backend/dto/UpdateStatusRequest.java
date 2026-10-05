package com.poms.backend.dto;

/**
 * Request body for PATCH /api/purchase-orders/:id/status
 */
public class UpdateStatusRequest {

    private String status;

    public UpdateStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
