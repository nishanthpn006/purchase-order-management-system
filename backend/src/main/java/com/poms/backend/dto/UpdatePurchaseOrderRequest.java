package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for PUT /api/purchase-orders/{id}
 */
@Schema(description = "Purchase order update payload including editable header fields and line items")
public class UpdatePurchaseOrderRequest {

    @Schema(description = "ID of the vendor supplier", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer vendorId;

    @Schema(description = "Date the order was placed (YYYY-MM-DD)", example = "2026-10-05", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate orderDate;

    @Schema(description = "Expected delivery date (YYYY-MM-DD)", example = "2026-10-20")
    private LocalDate expectedDelivery;

    @Schema(description = "List of line items included in the purchase order", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;

    public UpdatePurchaseOrderRequest() {
    }

    public UpdatePurchaseOrderRequest(Integer vendorId, LocalDate orderDate, LocalDate expectedDelivery, List<OrderItemRequest> items) {
        this.vendorId = vendorId;
        this.orderDate = orderDate;
        this.expectedDelivery = expectedDelivery;
        this.items = items;
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

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    /**
     * Line item detail for purchase order update.
     */
    @Schema(description = "Line item detail for purchase order update")
    public static class OrderItemRequest {

        @Schema(description = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer productId;

        @Schema(description = "Quantity ordered", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer quantity;

        @Schema(description = "Unit price of the product", example = "12000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal unitPrice;

        public OrderItemRequest() {
        }

        public OrderItemRequest(Integer productId, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
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
