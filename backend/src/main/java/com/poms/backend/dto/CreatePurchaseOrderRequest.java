package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for POST /api/purchase-orders.
 * Contains the PO header fields plus a list of line items.
 */
@Schema(description = "Purchase order creation payload including header fields and line items")
public class CreatePurchaseOrderRequest {

    @Schema(description = "ID of the vendor supplier", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer vendorId;

    @Schema(description = "Date the order was placed (YYYY-MM-DD)", example = "2026-10-05", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate orderDate;

    @Schema(description = "Expected delivery date (YYYY-MM-DD)", example = "2026-10-20")
    private LocalDate expectedDelivery;

    @Schema(description = "Total purchase order amount (optional; calculated authoritative value computed by server from line items)", example = "60000.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal totalAmount;

    @Schema(description = "Initial PO status (defaults to Pending)", example = "Pending", allowableValues = {"Pending", "Approved", "Rejected", "Completed"})
    private String status;

    @Schema(description = "List of line items included in the purchase order")
    private List<OrderItemRequest> items;

    public CreatePurchaseOrderRequest() {
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(LocalDate expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    /**
     * Nested DTO for individual line items in the create-PO request.
     */
    @Schema(description = "Line item detail for purchase order")
    public static class OrderItemRequest {

        @Schema(description = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer productId;

        @Schema(description = "Quantity ordered", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quantity;

        @Schema(description = "Unit price of the product", example = "12000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal unitPrice;

        public OrderItemRequest() {
        }

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
