package com.poms.backend.service;

import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.PurchaseOrderItemId;
import com.poms.backend.repository.PurchaseOrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderItemServiceTest {

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @InjectMocks
    private PurchaseOrderItemService purchaseOrderItemService;

    private PurchaseOrderItem sampleItem;
    private PurchaseOrderItemId sampleId;

    @BeforeEach
    void setUp() {
        sampleId = new PurchaseOrderItemId(1, 100);

        sampleItem = new PurchaseOrderItem();
        sampleItem.setId(sampleId);
        sampleItem.setQuantity(5);
        sampleItem.setUnitPrice(new BigDecimal("1200.00"));
        sampleItem.setTotalPrice(new BigDecimal("6000.00"));
    }

    @Test
    @DisplayName("getAllPurchaseOrderItems returns item list when items exist")
    void getAllPurchaseOrderItems_WhenItemsExist_ReturnsItemList() {
        when(purchaseOrderItemRepository.findAll()).thenReturn(List.of(sampleItem));

        List<PurchaseOrderItem> result = purchaseOrderItemService.getAllPurchaseOrderItems();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getQuantity());
        verify(purchaseOrderItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllPurchaseOrderItems returns empty list when no items exist")
    void getAllPurchaseOrderItems_WhenNoItemsExist_ReturnsEmptyList() {
        when(purchaseOrderItemRepository.findAll()).thenReturn(Collections.emptyList());

        List<PurchaseOrderItem> result = purchaseOrderItemService.getAllPurchaseOrderItems();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseOrderItemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getPurchaseOrderItemById returns item when item exists")
    void getPurchaseOrderItemById_WhenItemExists_ReturnsItem() {
        when(purchaseOrderItemRepository.findById(sampleId)).thenReturn(Optional.of(sampleItem));

        Optional<PurchaseOrderItem> result = purchaseOrderItemService.getPurchaseOrderItemById(sampleId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId().getPurchaseOrderId());
        assertEquals(100, result.get().getId().getProductId());
        assertEquals(new BigDecimal("1200.00"), result.get().getUnitPrice());
        verify(purchaseOrderItemRepository, times(1)).findById(sampleId);
    }

    @Test
    @DisplayName("getPurchaseOrderItemById returns empty Optional when item does not exist")
    void getPurchaseOrderItemById_WhenItemDoesNotExist_ReturnsEmpty() {
        PurchaseOrderItemId nonExistentId = new PurchaseOrderItemId(999, 888);
        when(purchaseOrderItemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<PurchaseOrderItem> result = purchaseOrderItemService.getPurchaseOrderItemById(nonExistentId);

        assertFalse(result.isPresent());
        verify(purchaseOrderItemRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("getItemsByPurchaseOrderId returns items belonging to purchase order")
    void getItemsByPurchaseOrderId_WhenItemsExist_ReturnsList() {
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(1)).thenReturn(List.of(sampleItem));

        List<PurchaseOrderItem> result = purchaseOrderItemService.getItemsByPurchaseOrderId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId().getPurchaseOrderId());
        verify(purchaseOrderItemRepository, times(1)).findByIdPurchaseOrderId(1);
    }

    @Test
    @DisplayName("getItemsByPurchaseOrderId returns empty list when no items belong to purchase order")
    void getItemsByPurchaseOrderId_WhenNoItemsExist_ReturnsEmptyList() {
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(999)).thenReturn(Collections.emptyList());

        List<PurchaseOrderItem> result = purchaseOrderItemService.getItemsByPurchaseOrderId(999);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseOrderItemRepository, times(1)).findByIdPurchaseOrderId(999);
    }

    @Test
    @DisplayName("savePurchaseOrderItem persists and returns saved purchase order item")
    void savePurchaseOrderItem_Success_ReturnsSavedItem() {
        when(purchaseOrderItemRepository.save(sampleItem)).thenReturn(sampleItem);

        PurchaseOrderItem result = purchaseOrderItemService.savePurchaseOrderItem(sampleItem);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("1200.00"), result.getUnitPrice());
        verify(purchaseOrderItemRepository, times(1)).save(sampleItem);
    }
}
