package com.poms.backend.controller;

import com.poms.backend.dto.CreatePurchaseOrderRequest;
import com.poms.backend.dto.UpdateStatusRequest;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.PurchaseOrderItemId;
import com.poms.backend.entity.User;
import com.poms.backend.security.JwtUtil;
import com.poms.backend.service.PurchaseOrderItemService;
import com.poms.backend.service.PurchaseOrderService;
import com.poms.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/purchase-orders")
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
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    /**
     * GET /api/purchase-orders/:id
     * Returns a single purchase order with its line items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPurchaseOrderById(@PathVariable Integer id) {
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
     * POST /api/purchase-orders
     * Creates a new purchase order with its line items.
     * Allowed roles: Admin, Manager, Employee.
     */
    @PostMapping
    public ResponseEntity<?> createPurchaseOrder(
            @RequestBody CreatePurchaseOrderRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Basic validation
        if (request.getVendorId() == null || request.getOrderDate() == null
                || request.getTotalAmount() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "vendorId, orderDate, and totalAmount are required"));
        }

        // Extract the current user from the JWT so we can set createdBy
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated user not found"));
        }

        // Generate PO number: PO-<timestamp>
        String poNumber = "PO-" + System.currentTimeMillis();

        // Build and save the PurchaseOrder header
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNumber);
        po.setVendorId(request.getVendorId());
        po.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        po.setExpectedDelivery(request.getExpectedDelivery());
        po.setTotalAmount(request.getTotalAmount());
        po.setStatus(request.getStatus() != null ? request.getStatus() : "Pending");
        po.setCreatedBy(userOpt.get().getId());
        po.setCreatedAt(LocalDateTime.now());

        PurchaseOrder savedPo = purchaseOrderService.savePurchaseOrder(po);

        // Save line items if provided
        if (request.getItems() != null) {
            for (CreatePurchaseOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                PurchaseOrderItemId itemId = new PurchaseOrderItemId(
                        savedPo.getId(), itemReq.getProductId());
                item.setId(itemId);
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                purchaseOrderItemService.savePurchaseOrderItem(item);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPo);
    }

    /**
     * PATCH /api/purchase-orders/:id/status
     * Updates the status of a purchase order.
     * Allowed roles: Admin, Manager only (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestBody UpdateStatusRequest request) {
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "status field is required"));
        }

        Optional<PurchaseOrder> poOpt = purchaseOrderService.getPurchaseOrderById(id);
        if (poOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Purchase order not found"));
        }

        PurchaseOrder po = poOpt.get();
        po.setStatus(request.getStatus());
        PurchaseOrder updated = purchaseOrderService.savePurchaseOrder(po);
        return ResponseEntity.ok(updated);
    }
}
