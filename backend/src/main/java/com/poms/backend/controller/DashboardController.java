package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.dto.DashboardStats;
import com.poms.backend.entity.Inventory;
import com.poms.backend.repository.InventoryRepository;
import com.poms.backend.repository.ProductRepository;
import com.poms.backend.repository.PurchaseOrderRepository;
import com.poms.backend.repository.VendorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Procurement operations overview and KPI statistics")
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
    @Operation(
            summary = "Get procurement dashboard statistics",
            description = "Protected endpoint. Returns aggregated counts of total purchase orders, pending orders, approved orders, received orders, total vendors, total products, and low stock inventory items. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard metrics calculated and returned successfully",
                    content = @Content(schema = @Schema(implementation = DashboardStats.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
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
