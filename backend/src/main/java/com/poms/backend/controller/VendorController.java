package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.dto.CreateVendorRequest;
import com.poms.backend.dto.UpdateVendorRequest;
import com.poms.backend.entity.Vendor;
import com.poms.backend.service.VendorService;
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
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendors", description = "Supplier and vendor account management endpoints")
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
    @Operation(
            summary = "List all vendors",
            description = "Protected endpoint. Returns a list of all registered supplier vendors including contact person, GST number, and status. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of vendors retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Vendor.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    /**
     * GET /api/vendors/:id
     * Returns a single vendor by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get vendor by ID",
            description = "Protected endpoint. Retrieves detailed information for a single vendor by their primary key ID. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vendor record found and returned",
                    content = @Content(schema = @Schema(implementation = Vendor.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "404", description = "Vendor not found with specified ID")
    })
    public ResponseEntity<?> getVendorById(
            @Parameter(description = "Primary key ID of the vendor", required = true, example = "1")
            @PathVariable Integer id) {
        return vendorService.getVendorById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Vendor not found")));
    }

    /**
     * POST /api/vendors
     * Creates a new vendor record.
     * Allowed roles: ADMIN, MANAGER.
     */
    @PostMapping
    @Operation(
            summary = "Create a new vendor",
            description = "Protected endpoint. Creates a new supplier vendor profile. Allowed roles: ADMIN, MANAGER."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Vendor created successfully",
                    content = @Content(schema = @Schema(implementation = Vendor.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error on request fields"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can create vendors")
    })
    public ResponseEntity<?> createVendor(@RequestBody CreateVendorRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }
        try {
            Vendor created = vendorService.createVendor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/vendors/:id
     * Updates an existing vendor record.
     * Allowed roles: ADMIN, MANAGER.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing vendor",
            description = "Protected endpoint. Updates an existing supplier vendor profile. "
                    + "ROLE RESTRICTION: Only users with ADMIN or MANAGER role are authorized. Users with EMPLOYEE role will receive HTTP 403 Forbidden."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vendor updated successfully",
                    content = @Content(schema = @Schema(implementation = Vendor.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error on request fields"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can update vendors"),
            @ApiResponse(responseCode = "404", description = "Vendor not found with specified ID")
    })
    public ResponseEntity<?> updateVendor(
            @Parameter(description = "Primary key ID of the vendor to update", required = true, example = "1")
            @PathVariable Integer id,
            @RequestBody UpdateVendorRequest request) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vendor ID is required"));
        }
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }
        try {
            Vendor updated = vendorService.updateVendor(id, request);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
