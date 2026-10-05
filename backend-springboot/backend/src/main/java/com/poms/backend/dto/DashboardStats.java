package com.poms.backend.dto;

/**
 * Response for GET /api/dashboard/stats
 */
public class DashboardStats {

    private long totalPurchaseOrders;
    private long pendingOrders;
    private long approvedOrders;
    private long receivedOrders;
    private long totalVendors;
    private long totalProducts;
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
