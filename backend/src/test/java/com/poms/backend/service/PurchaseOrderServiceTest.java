package com.poms.backend.service;

import com.poms.backend.dto.CreatePurchaseOrderRequest;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.repository.PurchaseOrderItemRepository;
import com.poms.backend.repository.PurchaseOrderRepository;
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
    @DisplayName("updateStatus rejects unknown or unallowed status such as Cancelled")
    void updateStatus_DisallowsCancelledOrUnknownStatus() {
        sampleOrder.setStatus("Pending");
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                purchaseOrderService.updateStatus(1, "Cancelled")
        );

        assertTrue(ex.getMessage().contains("Invalid status: 'Cancelled'"));
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
}
