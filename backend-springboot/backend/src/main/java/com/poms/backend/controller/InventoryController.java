package com.poms.backend.controller;

import com.poms.backend.entity.Inventory;
import com.poms.backend.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
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
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    /**
     * GET /api/inventory/:id
     * Returns a single inventory record by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable Integer id) {
        return inventoryService.getInventoryById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Inventory record not found")));
    }
}
