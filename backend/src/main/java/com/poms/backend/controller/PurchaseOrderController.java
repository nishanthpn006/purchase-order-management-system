package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.dto.CreatePurchaseOrderRequest;
import com.poms.backend.dto.PurchaseOrderReceivingDetailsResponse;
import com.poms.backend.dto.UpdatePurchaseOrderRequest;
import com.poms.backend.dto.UpdateStatusRequest;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.PurchaseOrderItemId;
import com.poms.backend.entity.User;
import com.poms.backend.security.JwtUtil;
import com.poms.backend.service.PurchaseOrderItemService;
import com.poms.backend.service.PurchaseOrderService;
import com.poms.backend.service.UserService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "Purchase Orders", description = "Purchase order creation, lifecycle management, and status approval workflows")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderItemService purchaseOrderItemService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
                                   PurchaseOrderItemService purchaseOrderItemService,
                                   UserService userService,
                                   JwtUtil jwtUtil) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseOrderItemService = purchaseOrderItemService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * GET /api/purchase-orders
     * Returns all purchase orders. Requires authentication.
     */
    @GetMapping
    @Operation(
            summary = "List all purchase orders",
            description = "Protected endpoint. Retrieves a list of all purchase orders across all statuses. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of purchase orders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PurchaseOrder.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    /**
     * GET /api/purchase-orders/:id
     * Returns a single purchase order with its line items.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get purchase order by ID with line items",
            description = "Protected endpoint. Returns complete purchase order header details together with its line items (composite key: purchase_order_id + product_id). Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Purchase order and line items found and returned successfully"
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found with specified ID")
    })
    public ResponseEntity<?> getPurchaseOrderById(
            @Parameter(description = "Primary key ID of the purchase order", required = true, example = "1")
            @PathVariable Integer id) {
        Optional<PurchaseOrder> poOpt = purchaseOrderService.getPurchaseOrderById(id);
        if (poOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase order not found"));
        }

        PurchaseOrder po = poOpt.get();
        List<PurchaseOrderItem> items = purchaseOrderItemService.getItemsByPurchaseOrderId(id);

        // Build a combined response map
        Map<String, Object> response = new HashMap<>();
        response.put("id", po.getId());
        response.put("poNumber", po.getPoNumber());
        response.put("vendorId", po.getVendorId());
        response.put("orderDate", po.getOrderDate());
        response.put("expectedDelivery", po.getExpectedDelivery());
        response.put("totalAmount", po.getTotalAmount());
        response.put("status", po.getStatus());
        response.put("createdBy", po.getCreatedBy());
        response.put("createdAt", po.getCreatedAt());
        response.put("items", items);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/purchase-orders/:id/receiving-details
     * Returns purchase order header information, vendor name, and line items with received and remaining quantities.
     * Used by Goods Receipt receiving modal.
     * Allowed roles: ADMIN, MANAGER, EMPLOYEE.
     */
    @GetMapping("/{id}/receiving-details")
    @Operation(
            summary = "Get purchase order receiving details",
            description = "Protected endpoint. Returns purchase order details with vendor name and line items showing ordered, received, and remaining quantities for Goods Receipt creation. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Purchase order receiving details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderReceivingDetailsResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found with specified ID")
    })
    public ResponseEntity<?> getPurchaseOrderReceivingDetails(
            @Parameter(description = "Primary key ID of the purchase order", required = true, example = "10")
            @PathVariable Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Purchase order ID is required"));
        }
        try {
            PurchaseOrderReceivingDetailsResponse response = purchaseOrderService.getPurchaseOrderReceivingDetails(id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Purchase order not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/purchase-orders
     * Creates a new purchase order with its line items.
     * Allowed roles: Admin, Manager, Employee.
     */
    @PostMapping
    @Operation(
            summary = "Create a new purchase order",
            description = "Protected endpoint. Creates a new purchase order with line items. The createdBy user is automatically identified from the JWT Bearer token. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Purchase order created successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrder.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error, missing required fields, or duplicate product IDs"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<?> createPurchaseOrder(
            @RequestBody CreatePurchaseOrderRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }

        // Basic validation
        if (request.getVendorId() == null || request.getOrderDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "vendorId and orderDate are required"));
        }

        // Extract the current user from the JWT Bearer token or security context
        String email = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(token);
            } catch (Exception ignored) {
            }
        }
        if (email == null) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                email = auth.getName();
            }
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated user not found"));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated user not found"));
        }

        try {
            PurchaseOrder savedPo = purchaseOrderService.createPurchaseOrder(request, userOpt.get().getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/purchase-orders/:id/status
     * Updates the status of a purchase order adhering to state machine transition rules.
     * Allowed roles: Admin, Manager only (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update purchase order status",
            description = "Protected endpoint. Updates the status (Pending, Approved, Rejected, Completed) of a purchase order adhering to state machine rules. "
                    + "ROLE RESTRICTION: Only users with ADMIN or MANAGER role are authorized. Users with EMPLOYEE role will receive HTTP 403 Forbidden."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Purchase order status updated successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrder.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Missing status or invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can update PO status"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found with specified ID")
    })
    public ResponseEntity<?> updateStatus(
            @Parameter(description = "Primary key ID of the purchase order to update", required = true, example = "1")
            @PathVariable Integer id,
            @RequestBody UpdateStatusRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "status field is required"));
        }

        try {
            PurchaseOrder updated = purchaseOrderService.updateStatus(id, request.getStatus());
            return ResponseEntity.ok(updated);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase order not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/purchase-orders/:id/cancel
     * Cancels an existing purchase order.
     * Cancellation is only permitted from 'Pending' or 'Approved' statuses.
     * Allowed roles: Admin, Manager only (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a purchase order",
            description = "Protected endpoint. Cancels a purchase order. Cancellation is only permitted from 'Pending' or 'Approved' statuses. "
                    + "ROLE RESTRICTION: Only users with ADMIN or MANAGER role are authorized. Users with EMPLOYEE role will receive HTTP 403 Forbidden."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Purchase order cancelled successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrder.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Cannot cancel purchase order from current status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can cancel purchase orders"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found with specified ID")
    })
    public ResponseEntity<?> cancelPurchaseOrder(
            @Parameter(description = "Primary key ID of the purchase order to cancel", required = true, example = "1")
            @PathVariable Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Purchase order ID is required"));
        }
        try {
            PurchaseOrder cancelled = purchaseOrderService.cancelPurchaseOrder(id);
            return ResponseEntity.ok(cancelled);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase order not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/purchase-orders/:id
     * Updates an existing purchase order and its line items.
     * Editing is allowed only when current status is 'Pending'.
     * Allowed roles: Admin, Manager only (enforced in SecurityConfig).
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing purchase order",
            description = "Protected endpoint. Updates an existing purchase order and its line items. Editing is only permitted when status is 'Pending'. Preserves immutable metadata while recalculating totalAmount on the server. "
                    + "ROLE RESTRICTION: Only users with ADMIN or MANAGER role are authorized. Users with EMPLOYEE role will receive HTTP 403 Forbidden."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Purchase order updated successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseOrder.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error, invalid status, duplicate products, or invalid vendor/product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN and MANAGER roles can edit purchase orders"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found with specified ID")
    })
    public ResponseEntity<?> updatePurchaseOrder(
            @Parameter(description = "Primary key ID of the purchase order to update", required = true, example = "1")
            @PathVariable Integer id,
            @RequestBody UpdatePurchaseOrderRequest request) {
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Purchase order ID is required"));
        }
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }
        try {
            PurchaseOrder updated = purchaseOrderService.updatePurchaseOrder(id, request);
            return ResponseEntity.ok(updated);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase order not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
