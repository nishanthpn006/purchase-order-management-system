package com.poms.backend.controller;

import com.poms.backend.dto.DashboardStats;
import com.poms.backend.entity.Inventory;
import com.poms.backend.repository.InventoryRepository;
import com.poms.backend.repository.ProductRepository;
import com.poms.backend.repository.PurchaseOrderRepository;
import com.poms.backend.repository.VendorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public DashboardController(PurchaseOrderRepository purchaseOrderRepository,
                               VendorRepository vendorRepository,
                               ProductRepository productRepository,
                               InventoryRepository inventoryRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * GET /api/dashboard/stats
     * Returns summary statistics for the dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = new DashboardStats();

        stats.setTotalPurchaseOrders(purchaseOrderRepository.count());
        stats.setPendingOrders(purchaseOrderRepository.countByStatus("Pending"));
        stats.setApprovedOrders(purchaseOrderRepository.countByStatus("Approved"));
        stats.setReceivedOrders(purchaseOrderRepository.countByStatus("Received"));
        stats.setTotalVendors(vendorRepository.count());
        stats.setTotalProducts(productRepository.count());

        // Count inventory items where quantity_in_stock <= reorder_level
        List<Inventory> allInventory = inventoryRepository.findAll();
        long lowStock = allInventory.stream()
                .filter(inv -> inv.getQuantityInStock() <= inv.getReorderLevel())
                .count();
        stats.setLowStockItems(lowStock);

        return ResponseEntity.ok(stats);
    }
}
