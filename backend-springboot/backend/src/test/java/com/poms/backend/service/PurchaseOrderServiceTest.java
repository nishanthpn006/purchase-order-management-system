package com.poms.backend.service;

import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

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
}
