package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response for GET /api/dashboard/stats
 */
@Schema(description = "Aggregated procurement KPIs and operational statistics")
public class DashboardStats {

    @Schema(description = "Total number of purchase orders", example = "25")
    private long totalPurchaseOrders;

    @Schema(description = "Total purchase orders in Pending status", example = "5")
    private long pendingOrders;

    @Schema(description = "Total purchase orders in Approved status", example = "12")
    private long approvedOrders;

    @Schema(description = "Total purchase orders in Received status", example = "8")
    private long receivedOrders;

    @Schema(description = "Total registered supplier vendors", example = "10")
    private long totalVendors;

    @Schema(description = "Total products cataloged in the system", example = "50")
    private long totalProducts;

    @Schema(description = "Count of products where current stock is at or below reorder level", example = "3")
    private long lowStockItems;

    public DashboardStats() {
    }

    public long getTotalPurchaseOrders() {
        return totalPurchaseOrders;
    }

    public void setTotalPurchaseOrders(long totalPurchaseOrders) {
        this.totalPurchaseOrders = totalPurchaseOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getApprovedOrders() {
        return approvedOrders;
    }

    public void setApprovedOrders(long approvedOrders) {
        this.approvedOrders = approvedOrders;
    }

    public long getReceivedOrders() {
        return receivedOrders;
    }

    public void setReceivedOrders(long receivedOrders) {
        this.receivedOrders = receivedOrders;
    }

    public long getTotalVendors() {
        return totalVendors;
    }

    public void setTotalVendors(long totalVendors) {
        this.totalVendors = totalVendors;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
}
