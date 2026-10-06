package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.entity.Inventory;
import com.poms.backend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Product stock levels, warehouse inventory, and reorder threshold monitoring")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * GET /api/inventory
     * Returns all inventory records.
     */
    @GetMapping
    @Operation(
            summary = "List all inventory records",
            description = "Protected endpoint. Retrieves all inventory stock entries including product ID, quantity in stock, reorder level, and last updated timestamp. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory records retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Inventory.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    /**
     * GET /api/inventory/:id
     * Returns a single inventory record by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get inventory record by ID",
            description = "Protected endpoint. Retrieves a specific inventory stock record by its primary key ID. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory record found and returned",
                    content = @Content(schema = @Schema(implementation = Inventory.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "404", description = "Inventory record not found with specified ID")
    })
    public ResponseEntity<?> getInventoryById(
            @Parameter(description = "Primary key ID of the inventory record", required = true, example = "1")
            @PathVariable Integer id) {
        return inventoryService.getInventoryById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Inventory record not found")));
    }
}
