package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase order receiving details response for Goods Receipt modal.
 * GET /api/purchase-orders/{id}/receiving-details
 */
@Schema(description = "Purchase order receiving details response for Goods Receipt modal")
public class PurchaseOrderReceivingDetailsResponse {

    @Schema(description = "Primary key ID of the purchase order", example = "10")
    private Integer purchaseOrderId;

    @Schema(description = "Purchase order reference number", example = "PO-0010")
    private String poNumber;

    @Schema(description = "Current lifecycle status of the purchase order", example = "Approved")
    private String status;

    @Schema(description = "Vendor ID associated with the purchase order", example = "3")
    private Integer vendorId;

    @Schema(description = "Vendor name resolved from the vendor ID", example = "Dell")
    private String vendorName;

    @Schema(description = "Date the purchase order was placed", example = "2026-10-09")
    private LocalDate orderDate;

    @Schema(description = "List of purchase order line items with receipt progress")
    private List<PurchaseOrderReceivingItemResponse> items = new ArrayList<>();

    public PurchaseOrderReceivingDetailsResponse() {
    }

    public PurchaseOrderReceivingDetailsResponse(Integer purchaseOrderId,
                                                 String poNumber,
                                                 String status,
                                                 Integer vendorId,
                                                 String vendorName,
                                                 LocalDate orderDate,
                                                 List<PurchaseOrderReceivingItemResponse> items) {
        this.purchaseOrderId = purchaseOrderId;
        this.poNumber = poNumber;
        this.status = status;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.orderDate = orderDate;
        this.items = items != null ? items : new ArrayList<>();
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public List<PurchaseOrderReceivingItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderReceivingItemResponse> items) {
        this.items = items;
    }
}
