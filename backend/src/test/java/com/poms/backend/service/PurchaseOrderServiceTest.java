package com.poms.backend.service;

import com.poms.backend.dto.CreatePurchaseOrderRequest;
import com.poms.backend.dto.PurchaseOrderReceivingDetailsResponse;
import com.poms.backend.dto.PurchaseOrderReceivingItemResponse;
import com.poms.backend.dto.UpdatePurchaseOrderRequest;
import com.poms.backend.entity.Product;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.Vendor;
import com.poms.backend.repository.GoodsReceiptItemRepository;
import com.poms.backend.repository.ProductRepository;
import com.poms.backend.repository.PurchaseOrderItemRepository;
import com.poms.backend.repository.PurchaseOrderRepository;
import com.poms.backend.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GoodsReceiptItemRepository goodsReceiptItemRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new PurchaseOrder();
        sampleOrder.setId(1);
        sampleOrder.setPoNumber("PO-2026-001");
        sampleOrder.setVendorId(10);
        sampleOrder.setOrderDate(LocalDate.of(2026, 3, 1));
        sampleOrder.setExpectedDelivery(LocalDate.of(2026, 3, 15));
        sampleOrder.setTotalAmount(new BigDecimal("150000.00"));
        sampleOrder.setStatus("Pending");
        sampleOrder.setCreatedBy(1);
        sampleOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllPurchaseOrders returns orders list when orders exist")
    void getAllPurchaseOrders_WhenOrdersExist_ReturnsOrderList() {
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(sampleOrder));

        List<PurchaseOrder> result = purchaseOrderService.getAllPurchaseOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PO-2026-001", result.get(0).getPoNumber());
        assertEquals("Pending", result.get(0).getStatus());
        verify(purchaseOrderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllPurchaseOrders returns empty list when no orders exist")
    void getAllPurchaseOrders_WhenNoOrdersExist_ReturnsEmptyList() {
        when(purchaseOrderRepository.findAll()).thenReturn(Collections.emptyList());

        List<PurchaseOrder> result = purchaseOrderService.getAllPurchaseOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseOrderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getPurchaseOrderById returns order when order exists")
    void getPurchaseOrderById_WhenOrderExists_ReturnsOrder() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Optional<PurchaseOrder> result = purchaseOrderService.getPurchaseOrderById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("PO-2026-001", result.get().getPoNumber());
        assertEquals(new BigDecimal("150000.00"), result.get().getTotalAmount());
        verify(purchaseOrderRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getPurchaseOrderById returns empty Optional when order does not exist")
    void getPurchaseOrderById_WhenOrderDoesNotExist_ReturnsEmpty() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());

        Optional<PurchaseOrder> result = purchaseOrderService.getPurchaseOrderById(999);

        assertFalse(result.isPresent());
        verify(purchaseOrderRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("savePurchaseOrder persists and returns saved purchase order")
    void savePurchaseOrder_Success_ReturnsSavedOrder() {
        when(purchaseOrderRepository.save(sampleOrder)).thenReturn(sampleOrder);

        PurchaseOrder result = purchaseOrderService.savePurchaseOrder(sampleOrder);

        assertNotNull(result);
        assertEquals("PO-2026-001", result.getPoNumber());
        assertEquals(new BigDecimal("150000.00"), result.getTotalAmount());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    // ── Milestone 1: Transaction-safe Purchase Order Creation Tests ──

    @Test
    @DisplayName("createPurchaseOrder method is annotated with @Transactional")
    void createPurchaseOrder_HasTransactionalAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderService.class.getMethod(
                "createPurchaseOrder", CreatePurchaseOrderRequest.class, Integer.class);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                "createPurchaseOrder must be annotated with @Transactional for atomicity");
    }

    @Test
    @DisplayName("createPurchaseOrder throws and propagates exception when item save fails (ensures rollback)")
    void createPurchaseOrder_WhenItemSaveFails_PropagatesException() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));

        CreatePurchaseOrderRequest.OrderItemRequest item = new CreatePurchaseOrderRequest.OrderItemRequest();
        item.setProductId(101);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("500.00"));
        request.setItems(List.of(item));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            po.setId(99);
            return po;
        });
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class)))
                .thenThrow(new RuntimeException("Simulated DB failure on item insert"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                purchaseOrderService.createPurchaseOrder(request, 1)
        );

        assertEquals("Simulated DB failure on item insert", ex.getMessage());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(purchaseOrderItemRepository, times(1)).save(any(PurchaseOrderItem.class));
    }

    // ── Milestone 2: Server-side Total Calculation Tests ──

    @Test
    @DisplayName("createPurchaseOrder calculates totalAmount server-side from submitted items")
    void createPurchaseOrder_CalculatesServerSideTotal() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));

        CreatePurchaseOrderRequest.OrderItemRequest item1 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item1.setProductId(101);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("150.00")); // 300.00

        CreatePurchaseOrderRequest.OrderItemRequest item2 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item2.setProductId(102);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("200.00")); // 600.00

        request.setItems(List.of(item1, item2));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder created = purchaseOrderService.createPurchaseOrder(request, 1);

        assertNotNull(created);
        assertEquals(new BigDecimal("900.00"), created.getTotalAmount());
        assertEquals("Pending", created.getStatus());
        verify(purchaseOrderItemRepository, times(2)).save(any(PurchaseOrderItem.class));
    }

    @Test
    @DisplayName("createPurchaseOrder ignores client-provided totalAmount and uses server-calculated sum")
    void createPurchaseOrder_IgnoresClientProvidedTotalAmount() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        // Client provides a tampered or incorrect totalAmount
        request.setTotalAmount(new BigDecimal("99999.00"));

        CreatePurchaseOrderRequest.OrderItemRequest item1 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item1.setProductId(101);
        item1.setQuantity(4);
        item1.setUnitPrice(new BigDecimal("50.00")); // 200.00
        request.setItems(List.of(item1));

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        when(purchaseOrderRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        purchaseOrderService.createPurchaseOrder(request, 1);

        PurchaseOrder persistedPo = captor.getValue();
        assertNotNull(persistedPo);
        assertEquals(new BigDecimal("200.00"), persistedPo.getTotalAmount(),
                "Server-calculated total must override any client-supplied totalAmount");
        assertNotEquals(new BigDecimal("99999.00"), persistedPo.getTotalAmount());
    }

    // ── Milestone 3: Duplicate Product Validation Tests ──

    @Test
    @DisplayName("createPurchaseOrder rejects request with duplicate product IDs with HTTP 400 (IllegalArgumentException)")
    void createPurchaseOrder_WithDuplicateProductIds_ThrowsIllegalArgumentException() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));

        CreatePurchaseOrderRequest.OrderItemRequest item1 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item1.setProductId(101);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("100.00"));

        CreatePurchaseOrderRequest.OrderItemRequest item2 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item2.setProductId(101); // Duplicate productId
        item2.setQuantity(5);
        item2.setUnitPrice(new BigDecimal("100.00"));

        request.setItems(List.of(item1, item2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.createPurchaseOrder(request, 1)
        );

        assertTrue(ex.getMessage().contains("Duplicate product ID"),
                "Expected error message mentioning duplicate product ID, got: " + ex.getMessage());
        verify(purchaseOrderRepository, never()).save(any());
        verify(purchaseOrderItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPurchaseOrder throws IllegalArgumentException when vendorId is missing")
    void createPurchaseOrder_MissingVendorId_ThrowsException() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setOrderDate(LocalDate.of(2026, 3, 1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.createPurchaseOrder(request, 1)
        );

        assertEquals("Vendor ID is required", ex.getMessage());
    }

    @Test
    @DisplayName("createPurchaseOrder throws IllegalArgumentException when orderDate is missing")
    void createPurchaseOrder_MissingOrderDate_ThrowsException() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.createPurchaseOrder(request, 1)
        );

        assertEquals("Order date is required", ex.getMessage());
    }

    // ── Milestone 4: Purchase Order Status Transition Validation Tests ──

    @Test
    @DisplayName("updateStatus allows valid transition: Pending -> Approved")
    void updateStatus_ValidTransition_PendingToApproved() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder updated = purchaseOrderService.updateStatus(1, "Approved");

        assertNotNull(updated);
        assertEquals("Approved", updated.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("updateStatus allows valid transition: Pending -> Rejected")
    void updateStatus_ValidTransition_PendingToRejected() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder updated = purchaseOrderService.updateStatus(1, "Rejected");

        assertNotNull(updated);
        assertEquals("Rejected", updated.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("updateStatus allows valid transition: Approved -> Completed")
    void updateStatus_ValidTransition_ApprovedToCompleted() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder updated = purchaseOrderService.updateStatus(1, "Completed");

        assertNotNull(updated);
        assertEquals("Completed", updated.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("updateStatus rejects invalid transition: Pending -> Completed")
    void updateStatus_InvalidTransition_PendingToCompleted() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Completed")
        );

        assertTrue(ex.getMessage().contains("Invalid status transition from 'Pending' to 'Completed'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus rejects invalid transition: Approved -> Pending")
    void updateStatus_InvalidTransition_ApprovedToPending() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Pending")
        );

        assertTrue(ex.getMessage().contains("Invalid status transition from 'Approved' to 'Pending'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus rejects invalid transition: Approved -> Rejected")
    void updateStatus_InvalidTransition_ApprovedToRejected() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Rejected")
        );

        assertTrue(ex.getMessage().contains("Invalid status transition from 'Approved' to 'Rejected'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus enforces terminal-state protection: Rejected cannot transition to Approved")
    void updateStatus_TerminalState_Rejected_CannotTransitionToApproved() {
        sampleOrder.setStatus("Rejected");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Approved")
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Rejected'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus enforces terminal-state protection: Rejected cannot transition to Completed")
    void updateStatus_TerminalState_Rejected_CannotTransitionToCompleted() {
        sampleOrder.setStatus("Rejected");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Completed")
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Rejected'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus enforces terminal-state protection: Completed cannot transition to Pending")
    void updateStatus_TerminalState_Completed_CannotTransitionToPending() {
        sampleOrder.setStatus("Completed");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Pending")
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Completed'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus enforces terminal-state protection: Completed cannot transition to Approved")
    void updateStatus_TerminalState_Completed_CannotTransitionToApproved() {
        sampleOrder.setStatus("Completed");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Approved")
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Completed'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus rejects unknown or unallowed status such as Shipped")
    void updateStatus_DisallowsUnknownStatus() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Shipped")
        );

        assertTrue(ex.getMessage().contains("Invalid status: 'Shipped'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus throws NoSuchElementException when purchase order ID is not found")
    void updateStatus_NotFound_ThrowsNoSuchElementException() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                purchaseOrderService.updateStatus(999, "Approved")
        );
    }

    // ── Milestone: Cancel Purchase Order Tests ──

    @Test
    @DisplayName("updateStatus allows valid transition: Pending -> Cancelled")
    void updateStatus_ValidTransition_PendingToCancelled() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder updated = purchaseOrderService.updateStatus(1, "Cancelled");

        assertNotNull(updated);
        assertEquals("Cancelled", updated.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("updateStatus allows valid transition: Approved -> Cancelled")
    void updateStatus_ValidTransition_ApprovedToCancelled() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder updated = purchaseOrderService.updateStatus(1, "Cancelled");

        assertNotNull(updated);
        assertEquals("Cancelled", updated.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("cancelPurchaseOrder allows cancelling a Pending purchase order")
    void cancelPurchaseOrder_FromPending_Succeeds() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder cancelled = purchaseOrderService.cancelPurchaseOrder(1);

        assertNotNull(cancelled);
        assertEquals("Cancelled", cancelled.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("cancelPurchaseOrder allows cancelling an Approved purchase order")
    void cancelPurchaseOrder_FromApproved_Succeeds() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder cancelled = purchaseOrderService.cancelPurchaseOrder(1);

        assertNotNull(cancelled);
        assertEquals("Cancelled", cancelled.getStatus());
        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
    }

    @Test
    @DisplayName("cancelPurchaseOrder throws IllegalArgumentException when cancelling a Rejected purchase order")
    void cancelPurchaseOrder_FromTerminal_Rejected_ThrowsException() {
        sampleOrder.setStatus("Rejected");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.cancelPurchaseOrder(1)
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Rejected'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelPurchaseOrder throws IllegalArgumentException when cancelling a Completed purchase order")
    void cancelPurchaseOrder_FromTerminal_Completed_ThrowsException() {
        sampleOrder.setStatus("Completed");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.cancelPurchaseOrder(1)
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Completed'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelPurchaseOrder throws IllegalArgumentException when cancelling an already Cancelled purchase order")
    void cancelPurchaseOrder_FromTerminal_Cancelled_ThrowsException() {
        sampleOrder.setStatus("Cancelled");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.cancelPurchaseOrder(1)
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Cancelled'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus enforces terminal-state protection: Cancelled cannot transition to any status")
    void updateStatus_TerminalState_Cancelled_CannotTransition() {
        sampleOrder.setStatus("Cancelled");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Pending")
        );

        assertTrue(ex.getMessage().contains("Cannot transition from terminal status 'Cancelled'"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelPurchaseOrder throws NoSuchElementException when PO ID does not exist")
    void cancelPurchaseOrder_NotFound_ThrowsNoSuchElementException() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                purchaseOrderService.cancelPurchaseOrder(999)
        );
    }

    // ── Milestone: Update Purchase Order (PUT) Tests ──

    @Test
    @DisplayName("updatePurchaseOrder method is annotated with @Transactional")
    void updatePurchaseOrder_HasTransactionalAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderService.class.getMethod(
                "updatePurchaseOrder", Integer.class, UpdatePurchaseOrderRequest.class);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                "updatePurchaseOrder must be annotated with @Transactional for atomicity");
    }

    @Test
    @DisplayName("updatePurchaseOrder successfully updates a Pending purchase order and recalculates totalAmount")
    void updatePurchaseOrder_SuccessfulPendingPoEdit() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(20)).thenReturn(true);
        when(productRepository.existsById(101)).thenReturn(true);
        when(productRepository.existsById(102)).thenReturn(true);

        PurchaseOrderItem existingItem = new PurchaseOrderItem();
        existingItem.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 101));
        existingItem.setQuantity(1);
        existingItem.setUnitPrice(new BigDecimal("100.00"));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(new java.util.ArrayList<>(List.of(existingItem)));

        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(20);
        request.setOrderDate(LocalDate.of(2026, 4, 1));
        request.setExpectedDelivery(LocalDate.of(2026, 4, 15));

        UpdatePurchaseOrderRequest.OrderItemRequest item1 = new UpdatePurchaseOrderRequest.OrderItemRequest(101, 3, new BigDecimal("100.00")); // 300.00
        UpdatePurchaseOrderRequest.OrderItemRequest item2 = new UpdatePurchaseOrderRequest.OrderItemRequest(102, 2, new BigDecimal("250.00")); // 500.00
        request.setItems(List.of(item1, item2));

        PurchaseOrder updated = purchaseOrderService.updatePurchaseOrder(1, request);

        assertNotNull(updated);
        assertEquals(1, updated.getId());
        assertEquals("PO-2026-001", updated.getPoNumber()); // preserved
        assertEquals("Pending", updated.getStatus()); // preserved
        assertEquals(20, updated.getVendorId()); // updated
        assertEquals(LocalDate.of(2026, 4, 1), updated.getOrderDate()); // updated
        assertEquals(LocalDate.of(2026, 4, 15), updated.getExpectedDelivery()); // updated
        assertEquals(new BigDecimal("800.00"), updated.getTotalAmount()); // recalculated

        verify(purchaseOrderRepository, times(1)).save(sampleOrder);
        verify(purchaseOrderItemRepository, times(2)).save(any(PurchaseOrderItem.class));
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit when PO status is Approved")
    void updatePurchaseOrder_RejectedForApproved() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 1, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Cannot edit purchase order in 'Approved' status"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit when PO status is Rejected")
    void updatePurchaseOrder_RejectedForRejected() {
        sampleOrder.setStatus("Rejected");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 1, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Cannot edit purchase order in 'Rejected' status"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit when PO status is Completed")
    void updatePurchaseOrder_RejectedForCompleted() {
        sampleOrder.setStatus("Completed");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 1, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Cannot edit purchase order in 'Completed' status"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit when PO status is Cancelled")
    void updatePurchaseOrder_RejectedForCancelled() {
        sampleOrder.setStatus("Cancelled");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 1, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Cannot edit purchase order in 'Cancelled' status"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit when vendor ID does not exist")
    void updatePurchaseOrder_InvalidVendor_ThrowsIllegalArgumentException() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(999)).thenReturn(false);

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(999);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 1, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Vendor not found with ID: 999"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit when a product ID does not exist")
    void updatePurchaseOrder_InvalidProduct_ThrowsIllegalArgumentException() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.existsById(888)).thenReturn(false);

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(888, 1, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Product not found with ID: 888"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit with duplicate product IDs")
    void updatePurchaseOrder_DuplicateProducts_ThrowsIllegalArgumentException() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.existsById(101)).thenReturn(true);

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(
                new UpdatePurchaseOrderRequest.OrderItemRequest(101, 1, new BigDecimal("10.00")),
                new UpdatePurchaseOrderRequest.OrderItemRequest(101, 2, new BigDecimal("10.00"))
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Duplicate product ID found in purchase order items: 101"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit with invalid quantity (<= 0)")
    void updatePurchaseOrder_InvalidQuantity_ThrowsIllegalArgumentException() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.existsById(101)).thenReturn(true);

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 0, new BigDecimal("10.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Quantity must be greater than zero for product ID: 101"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder rejects edit with invalid unit price (< 0)")
    void updatePurchaseOrder_InvalidPrice_ThrowsIllegalArgumentException() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.existsById(101)).thenReturn(true);

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 2, new BigDecimal("-5.00"))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertTrue(ex.getMessage().contains("Unit price cannot be negative for product ID: 101"));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseOrder propagates exception when line item save fails to ensure transaction rollback")
    void updatePurchaseOrder_TransactionRollbackWhereApplicable() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.existsById(101)).thenReturn(true);
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(Collections.emptyList());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class)))
                .thenThrow(new RuntimeException("Simulated item update DB failure"));

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);
        request.setOrderDate(LocalDate.of(2026, 3, 1));
        request.setItems(List.of(new UpdatePurchaseOrderRequest.OrderItemRequest(101, 2, new BigDecimal("100.00"))));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                purchaseOrderService.updatePurchaseOrder(1, request)
        );

        assertEquals("Simulated item update DB failure", ex.getMessage());
    }

    @Test
    @DisplayName("updatePurchaseOrder throws NoSuchElementException when PO ID is not found")
    void updatePurchaseOrder_NotFound_ThrowsNoSuchElementException() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());

        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setVendorId(10);

        assertThrows(NoSuchElementException.class, () ->
                purchaseOrderService.updatePurchaseOrder(999, request)
        );
    }

    // ── Milestone: Purchase Order Receiving Details Tests ──

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails returns zero received quantities when PO has no receipts")
    void getPurchaseOrderReceivingDetails_WithNoReceipts_ReturnsZeroReceivedQuantities() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Vendor vendor = new Vendor();
        vendor.setId(10);
        vendor.setVendorName("Dell Technologies");
        when(vendorRepository.findById(10)).thenReturn(Optional.of(vendor));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 101));
        item.setQuantity(20);
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(101);
        product.setProductName("Dell Latitude");
        when(productRepository.findById(101)).thenReturn(Optional.of(product));

        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(1)).thenReturn(Collections.emptyList());

        PurchaseOrderReceivingDetailsResponse details = purchaseOrderService.getPurchaseOrderReceivingDetails(1);

        assertNotNull(details);
        assertEquals(1, details.getPurchaseOrderId());
        assertEquals("PO-2026-001", details.getPoNumber());
        assertEquals("Pending", details.getStatus());
        assertEquals(10, details.getVendorId());
        assertEquals("Dell Technologies", details.getVendorName());
        assertEquals(LocalDate.of(2026, 3, 1), details.getOrderDate());
        assertEquals(1, details.getItems().size());

        PurchaseOrderReceivingItemResponse itemResp = details.getItems().get(0);
        assertEquals(101, itemResp.getProductId());
        assertEquals("Dell Latitude", itemResp.getProductName());
        assertEquals(20, itemResp.getOrderedQuantity());
        assertEquals(0, itemResp.getReceivedQuantity());
        assertEquals(20, itemResp.getRemainingQuantity());
    }

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails calculates correct remaining quantity for partially received PO")
    void getPurchaseOrderReceivingDetails_PartiallyReceived_ReturnsCorrectRemainingQuantities() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Vendor vendor = new Vendor();
        vendor.setId(10);
        vendor.setVendorName("Dell Technologies");
        when(vendorRepository.findById(10)).thenReturn(Optional.of(vendor));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 101));
        item.setQuantity(20);
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(101);
        product.setProductName("Dell Latitude");
        when(productRepository.findById(101)).thenReturn(Optional.of(product));

        // 12 previously received
        List<Object[]> sumList = List.<Object[]>of(new Object[]{101, 12L});
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(1)).thenReturn(sumList);

        PurchaseOrderReceivingDetailsResponse details = purchaseOrderService.getPurchaseOrderReceivingDetails(1);

        assertNotNull(details);
        assertEquals(1, details.getItems().size());
        PurchaseOrderReceivingItemResponse itemResp = details.getItems().get(0);
        assertEquals(101, itemResp.getProductId());
        assertEquals("Dell Latitude", itemResp.getProductName());
        assertEquals(20, itemResp.getOrderedQuantity());
        assertEquals(12, itemResp.getReceivedQuantity());
        assertEquals(8, itemResp.getRemainingQuantity());
    }

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails returns zero remaining quantity for fully received PO")
    void getPurchaseOrderReceivingDetails_FullyReceived_ReturnsZeroRemainingQuantities() {
        sampleOrder.setStatus("Completed");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Vendor vendor = new Vendor();
        vendor.setId(10);
        vendor.setVendorName("Dell Technologies");
        when(vendorRepository.findById(10)).thenReturn(Optional.of(vendor));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 101));
        item.setQuantity(10);
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(101);
        product.setProductName("Dell Latitude");
        when(productRepository.findById(101)).thenReturn(Optional.of(product));

        // 10 previously received
        List<Object[]> sumList = List.<Object[]>of(new Object[]{101, 10L});
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(1)).thenReturn(sumList);

        PurchaseOrderReceivingDetailsResponse details = purchaseOrderService.getPurchaseOrderReceivingDetails(1);

        assertNotNull(details);
        assertEquals(1, details.getItems().size());
        PurchaseOrderReceivingItemResponse itemResp = details.getItems().get(0);
        assertEquals(10, itemResp.getOrderedQuantity());
        assertEquals(10, itemResp.getReceivedQuantity());
        assertEquals(0, itemResp.getRemainingQuantity());
    }

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails calculates multiple lines independently")
    void getPurchaseOrderReceivingDetails_MultipleLines_CalculatedIndependently() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Vendor vendor = new Vendor();
        vendor.setId(10);
        vendor.setVendorName("Supplier Co");
        when(vendorRepository.findById(10)).thenReturn(Optional.of(vendor));

        PurchaseOrderItem item1 = new PurchaseOrderItem();
        item1.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 101));
        item1.setQuantity(10);

        PurchaseOrderItem item2 = new PurchaseOrderItem();
        item2.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 102));
        item2.setQuantity(20);

        PurchaseOrderItem item3 = new PurchaseOrderItem();
        item3.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 103));
        item3.setQuantity(15);

        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(List.of(item1, item2, item3));

        Product prod1 = new Product();
        prod1.setId(101);
        prod1.setProductName("Product A");

        Product prod2 = new Product();
        prod2.setId(102);
        prod2.setProductName("Product B");

        Product prod3 = new Product();
        prod3.setId(103);
        prod3.setProductName("Product C");

        when(productRepository.findById(101)).thenReturn(Optional.of(prod1));
        when(productRepository.findById(102)).thenReturn(Optional.of(prod2));
        when(productRepository.findById(103)).thenReturn(Optional.of(prod3));

        // Line 1: 4 received; Line 2: 20 received; Line 3: not received (missing from sum)
        List<Object[]> sumList = List.of(
                new Object[]{101, 4L},
                new Object[]{102, 20L}
        );
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(1)).thenReturn(sumList);

        PurchaseOrderReceivingDetailsResponse details = purchaseOrderService.getPurchaseOrderReceivingDetails(1);

        assertNotNull(details);
        assertEquals(3, details.getItems().size());

        // Line 1: 10 ordered, 4 received -> 6 remaining
        PurchaseOrderReceivingItemResponse r1 = details.getItems().get(0);
        assertEquals(101, r1.getProductId());
        assertEquals("Product A", r1.getProductName());
        assertEquals(10, r1.getOrderedQuantity());
        assertEquals(4, r1.getReceivedQuantity());
        assertEquals(6, r1.getRemainingQuantity());

        // Line 2: 20 ordered, 20 received -> 0 remaining
        PurchaseOrderReceivingItemResponse r2 = details.getItems().get(1);
        assertEquals(102, r2.getProductId());
        assertEquals("Product B", r2.getProductName());
        assertEquals(20, r2.getOrderedQuantity());
        assertEquals(20, r2.getReceivedQuantity());
        assertEquals(0, r2.getRemainingQuantity());

        // Line 3: 15 ordered, 0 received -> 15 remaining
        PurchaseOrderReceivingItemResponse r3 = details.getItems().get(2);
        assertEquals(103, r3.getProductId());
        assertEquals("Product C", r3.getProductName());
        assertEquals(15, r3.getOrderedQuantity());
        assertEquals(0, r3.getReceivedQuantity());
        assertEquals(15, r3.getRemainingQuantity());
    }

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails throws NoSuchElementException for non-existent PO")
    void getPurchaseOrderReceivingDetails_NonExistentPo_ThrowsNoSuchElementException() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                purchaseOrderService.getPurchaseOrderReceivingDetails(999)
        );

        assertTrue(ex.getMessage().contains("Purchase order not found with ID: 999"));
    }

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails never returns negative remaining quantity for inconsistent data")
    void getPurchaseOrderReceivingDetails_InconsistentData_RemainingNeverNegative() {
        sampleOrder.setStatus("Approved");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(new com.poms.backend.entity.PurchaseOrderItemId(1, 101));
        item.setQuantity(10);
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(List.of(item));

        Product product = new Product();
        product.setId(101);
        product.setProductName("Test Item");
        when(productRepository.findById(101)).thenReturn(Optional.of(product));

        // Inconsistent receipt sum: 15 received against 10 ordered
        List<Object[]> sumList = List.<Object[]>of(new Object[]{101, 15L});
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(1)).thenReturn(sumList);

        PurchaseOrderReceivingDetailsResponse details = purchaseOrderService.getPurchaseOrderReceivingDetails(1);

        assertNotNull(details);
        PurchaseOrderReceivingItemResponse itemResp = details.getItems().get(0);
        assertEquals(10, itemResp.getOrderedQuantity());
        assertEquals(15, itemResp.getReceivedQuantity());
        assertEquals(0, itemResp.getRemainingQuantity(), "Remaining quantity must not be negative");
    }

    @Test
    @DisplayName("getPurchaseOrderReceivingDetails performs no save or delete operations (read-only verification)")
    void getPurchaseOrderReceivingDetails_PerformsNoSaveOrUpdateOperations() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(Collections.emptyList());
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(1)).thenReturn(Collections.emptyList());

        purchaseOrderService.getPurchaseOrderReceivingDetails(1);

        verify(purchaseOrderRepository, never()).save(any());
        verify(purchaseOrderRepository, never()).delete(any());
        verify(purchaseOrderItemRepository, never()).save(any());
        verify(purchaseOrderItemRepository, never()).delete(any());
        verify(vendorRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(goodsReceiptItemRepository, never()).save(any());
    }
}
