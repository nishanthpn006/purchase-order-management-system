package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for creating a goods receipt against a purchase order.
 * POST /api/goods-receipts
 */
@Schema(description = "Goods receipt creation payload including purchase order reference, receipt date, and items")
public class CreateGoodsReceiptRequest {

    @NotNull(message = "Purchase order ID is required")
    @Schema(description = "ID of the associated purchase order", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer purchaseOrderId;

    @NotNull(message = "Received date is required")
    @Schema(description = "Date goods were received (YYYY-MM-DD)", example = "2026-10-08", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate receivedDate;

    @Schema(description = "Optional remarks or notes about the receipt", example = "Items received in good condition")
    private String remarks;

    @NotEmpty(message = "Goods receipt must contain at least one line item")
    @Valid
    @Schema(description = "List of items received in this receipt")
    private List<ReceiptItemRequest> items;

    public CreateGoodsReceiptRequest() {
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<ReceiptItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItemRequest> items) {
        this.items = items;
    }

    /**
     * Line item detail for goods receipt.
     */
    @Schema(description = "Line item detail for goods receipt")
    public static class ReceiptItemRequest {

        @NotNull(message = "Product ID is required")
        @Schema(description = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer productId;

        @NotNull(message = "Received quantity is required")
        @Min(value = 1, message = "Received quantity must be greater than zero")
        @Schema(description = "Quantity received", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer receivedQuantity;

        public ReceiptItemRequest() {
        }

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public Integer getReceivedQuantity() {
            return receivedQuantity;
        }

        public void setReceivedQuantity(Integer receivedQuantity) {
            this.receivedQuantity = receivedQuantity;
        }
    }
}
