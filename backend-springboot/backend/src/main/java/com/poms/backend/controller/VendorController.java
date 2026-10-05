package com.poms.backend.controller;

import com.poms.backend.entity.Vendor;
import com.poms.backend.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * GET /api/vendors
     * Returns a list of all vendors.
     */
    @GetMapping
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    /**
     * GET /api/vendors/:id
     * Returns a single vendor by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVendorById(@PathVariable Integer id) {
        return vendorService.getVendorById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Vendor not found")));
    }
}
