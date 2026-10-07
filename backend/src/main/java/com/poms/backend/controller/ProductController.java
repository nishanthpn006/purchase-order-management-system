package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.dto.CreateProductRequest;
import com.poms.backend.dto.UpdateProductRequest;
import com.poms.backend.entity.Product;
import com.poms.backend.service.ProductService;
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
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog, specifications, and pricing endpoints")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     * Returns a list of all products.
     */
    @GetMapping
    @Operation(
            summary = "List all products",
            description = "Protected endpoint. Returns the entire product catalog with categories, unit prices, units of measure, and associated vendor IDs. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products catalog retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * GET /api/products/:id
     * Returns a single product by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Protected endpoint. Retrieves detailed information for a specific product by its ID. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found and returned successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "404", description = "Product not found with specified ID")
    })
    public ResponseEntity<?> getProductById(
            @Parameter(description = "Primary key ID of the product", required = true, example = "1")
            @PathVariable Integer id) {
        return productService.getProductById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found")));
    }

    /**
     * POST /api/products
     * Creates a new product record.
     * Allowed roles: ADMIN, MANAGER.
     */
    @PostMapping
    @Operation(
            summary = "Create a new product",
            description = "Protected endpoint. Registers a new product item in the catalog linked to a valid supplier vendor. Allowed roles: ADMIN, MANAGER."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error or vendor not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can create products")
    })
    public ResponseEntity<?> createProduct(@RequestBody CreateProductRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }
        try {
            Product created = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/products/:id
     * Updates an existing product record.
     * Allowed roles: ADMIN, MANAGER.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing product",
            description = "Protected endpoint. Updates specifications, category, pricing, stock, or vendor assignment of an existing product in the catalog. "
                    + "ROLE RESTRICTION: Only users with ADMIN or MANAGER role are authorized. Users with EMPLOYEE role will receive HTTP 403 Forbidden."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error or invalid vendor"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can update products"),
            @ApiResponse(responseCode = "404", description = "Product not found with specified ID")
    })
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Primary key ID of the product to update", required = true, example = "1")
            @PathVariable Integer id,
            @RequestBody UpdateProductRequest request) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product ID is required"));
        }
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }
        try {
            Product updated = productService.updateProduct(id, request);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/products/:id/deactivate
     * Deactivates an existing product record by setting its status to Unavailable.
     * Allowed roles: ADMIN, MANAGER.
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Deactivate a product",
            description = "Protected endpoint. Deactivates a product by setting status to Unavailable without deleting its database record. "
                    + "ROLE RESTRICTION: Only users with ADMIN or MANAGER role are authorized. Users with EMPLOYEE role will receive HTTP 403 Forbidden."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product deactivated successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid product ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can deactivate products"),
            @ApiResponse(responseCode = "404", description = "Product not found with specified ID")
    })
    public ResponseEntity<?> deactivateProduct(
            @Parameter(description = "Primary key ID of the product to deactivate", required = true, example = "1")
            @PathVariable Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product ID is required"));
        }
        try {
            Product deactivated = productService.deactivateProduct(id);
            return ResponseEntity.ok(deactivated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
