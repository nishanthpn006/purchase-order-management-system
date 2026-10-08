package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for PATCH /api/purchase-orders/:id/status
 */
@Schema(description = "Purchase order status update payload")
public class UpdateStatusRequest {

    @Schema(
            description = "New purchase order status (Pending, Approved, Rejected, Completed)",
            example = "Approved",
            allowableValues = {"Pending", "Approved", "Rejected", "Completed", "Cancelled"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
