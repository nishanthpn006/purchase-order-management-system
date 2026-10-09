package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Line item receiving progress detail for a purchase order.
 */
@Schema(description = "Purchase order line item receiving progress detail")
public class PurchaseOrderReceivingItemResponse {

    @Schema(description = "Product ID", example = "1")
    private Integer productId;

    @Schema(description = "Product name resolved from product catalog", example = "Dell Latitude")
    private String productName;

    @Schema(description = "Total quantity ordered in the purchase order", example = "20")
    private Integer orderedQuantity;

    @Schema(description = "Total quantity received so far across all goods receipts", example = "12")
    private Integer receivedQuantity;

    @Schema(description = "Remaining quantity eligible to be received (minimum 0)", example = "8")
    private Integer remainingQuantity;

    public PurchaseOrderReceivingItemResponse() {
    }

    public PurchaseOrderReceivingItemResponse(Integer productId,
                                              String productName,
                                              Integer orderedQuantity,
                                              Integer receivedQuantity,
                                              Integer remainingQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.orderedQuantity = orderedQuantity;
        this.receivedQuantity = receivedQuantity;
        this.remainingQuantity = remainingQuantity;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Integer orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}
