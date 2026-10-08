package com.poms.backend.service;

import com.poms.backend.dto.CreatePurchaseOrderRequest;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.PurchaseOrderItemId;
import com.poms.backend.repository.PurchaseOrderItemRepository;
import com.poms.backend.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PurchaseOrderService {

    public static final Set<String> VALID_STATUSES = Set.of("Pending", "Approved", "Rejected", "Completed");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderItemRepository purchaseOrderItemRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public Optional<PurchaseOrder> getPurchaseOrderById(Integer id) {
        return purchaseOrderRepository.findById(id);
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrderRepository.save(purchaseOrder);
    }

    /**
     * Transaction-safe Purchase Order creation.
     * Header creation and all line-item inserts succeed or fail together.
     * Calculates the totalAmount authoritatively on the backend, ignoring any client-provided totalAmount.
     * Validates that no duplicate product IDs are present in the submitted items.
     */
    @Transactional
    public PurchaseOrder createPurchaseOrder(CreatePurchaseOrderRequest request, Integer createdBy) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.getVendorId() == null) {
            throw new IllegalArgumentException("Vendor ID is required");
        }
        if (request.getOrderDate() == null) {
            throw new IllegalArgumentException("Order date is required");
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            if (!"Pending".equalsIgnoreCase(request.getStatus().trim())) {
                throw new IllegalArgumentException("Initial purchase order status must be 'Pending'");
            }
        }

        // Validate items and detect duplicate product IDs
        if (request.getItems() != null) {
            Set<Integer> seenProductIds = new HashSet<>();
            for (CreatePurchaseOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                if (itemReq.getProductId() == null) {
                    throw new IllegalArgumentException("Product ID is required for all line items");
                }
                if (!seenProductIds.add(itemReq.getProductId())) {
                    throw new IllegalArgumentException("Duplicate product ID found in purchase order items: " + itemReq.getProductId());
                }
                if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than zero for product ID: " + itemReq.getProductId());
                }
                if (itemReq.getUnitPrice() == null || itemReq.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Unit price cannot be negative for product ID: " + itemReq.getProductId());
                }
            }
        }

        // Server-side authoritative total amount calculation: sum(quantity * unitPrice)
        // Client-supplied totalAmount is intentionally ignored
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreatePurchaseOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                BigDecimal itemQty = BigDecimal.valueOf(itemReq.getQuantity());
                BigDecimal lineTotal = itemReq.getUnitPrice().multiply(itemQty);
                calculatedTotal = calculatedTotal.add(lineTotal);
            }
        }
        calculatedTotal = calculatedTotal.setScale(2, RoundingMode.HALF_UP);

        String poNumber = "PO-" + System.currentTimeMillis();

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNumber);
        po.setVendorId(request.getVendorId());
        po.setOrderDate(request.getOrderDate());
        po.setExpectedDelivery(request.getExpectedDelivery());
        po.setTotalAmount(calculatedTotal);
        po.setStatus("Pending");
        po.setCreatedBy(createdBy);
        po.setCreatedAt(LocalDateTime.now());

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        if (request.getItems() != null) {
            for (CreatePurchaseOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                PurchaseOrderItemId itemId = new PurchaseOrderItemId(savedPo.getId(), itemReq.getProductId());
                item.setId(itemId);
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                purchaseOrderItemRepository.save(item);
            }
        }

        return savedPo;
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(CreatePurchaseOrderRequest request) {
        return createPurchaseOrder(request, null);
    }

    /**
     * Updates the status of an existing Purchase Order adhering to state machine transition rules.
     */
    @Transactional
    public PurchaseOrder updateStatus(Integer id, String newStatus) {
        if (id == null) {
            throw new IllegalArgumentException("Purchase order ID is required");
        }
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Status field is required");
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Purchase order not found with ID: " + id));

        validateStatusTransition(po.getStatus(), newStatus);

        po.setStatus(normalizeStatus(newStatus));
        return purchaseOrderRepository.save(po);
    }

    /**
     * State machine validation for PO status transitions.
     * Allowed transitions:
     *   - Pending -> Approved
     *   - Pending -> Rejected
     *   - Approved -> Completed
     * Terminal states (no further transitions allowed):
     *   - Rejected
     *   - Completed
     */
    public void validateStatusTransition(String currentStatus, String targetStatus) {
        if (targetStatus == null || targetStatus.isBlank()) {
            throw new IllegalArgumentException("Status field is required");
        }

        String normalizedTarget = normalizeStatus(targetStatus);
        if (normalizedTarget == null) {
            throw new IllegalArgumentException("Invalid status: '" + targetStatus + "'. Valid statuses are: Pending, Approved, Rejected, Completed");
        }

        if (currentStatus == null || currentStatus.isBlank()) {
            throw new IllegalArgumentException("Current status is not set");
        }

        String normalizedCurrent = normalizeStatus(currentStatus);
        if (normalizedCurrent == null) {
            throw new IllegalArgumentException("Invalid current status: '" + currentStatus + "'");
        }

        // Terminal states protection: Rejected and Completed allow no transitions
        if ("Rejected".equals(normalizedCurrent)) {
            throw new IllegalArgumentException("Cannot transition from terminal status 'Rejected'");
        }
        if ("Completed".equals(normalizedCurrent)) {
            throw new IllegalArgumentException("Cannot transition from terminal status 'Completed'");
        }

        // Allowed transitions:
        // Pending -> Approved, Pending -> Rejected
        // Approved -> Completed
        if ("Pending".equals(normalizedCurrent)) {
            if (!"Approved".equals(normalizedTarget) && !"Rejected".equals(normalizedTarget)) {
                throw new IllegalArgumentException("Invalid status transition from 'Pending' to '" + normalizedTarget + "'. Allowed transitions from 'Pending' are: Approved, Rejected");
            }
        } else if ("Approved".equals(normalizedCurrent)) {
            if (!"Completed".equals(normalizedTarget)) {
                throw new IllegalArgumentException("Invalid status transition from 'Approved' to '" + normalizedTarget + "'. Allowed transition from 'Approved' is: Completed");
            }
        } else {
            throw new IllegalArgumentException("Invalid status transition from '" + normalizedCurrent + "' to '" + normalizedTarget + "'");
        }
    }

    public String normalizeStatus(String status) {
        if (status == null) return null;
        for (String s : VALID_STATUSES) {
            if (s.equalsIgnoreCase(status.trim())) {
                return s;
            }
        }
        return null;
    }
}