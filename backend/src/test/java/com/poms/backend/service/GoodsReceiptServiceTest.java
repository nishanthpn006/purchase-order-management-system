package com.poms.backend.service;

import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.repository.GoodsReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @InjectMocks
    private GoodsReceiptService goodsReceiptService;

    private GoodsReceipt sampleReceipt;

    @BeforeEach
    void setUp() {
        sampleReceipt = new GoodsReceipt();
        sampleReceipt.setId(1);
        sampleReceipt.setPurchaseOrderId(5);
        sampleReceipt.setReceivedDate(LocalDate.of(2026, 3, 10));
        sampleReceipt.setReceivedBy(2);
        sampleReceipt.setRemarks("Received all items in good condition");
        sampleReceipt.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllGoodsReceipts returns receipt list when receipts exist")
    void getAllGoodsReceipts_WhenReceiptsExist_ReturnsList() {
        when(goodsReceiptRepository.findAll()).thenReturn(List.of(sampleReceipt));

        List<GoodsReceipt> result = goodsReceiptService.getAllGoodsReceipts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getPurchaseOrderId());
        assertEquals("Received all items in good condition", result.get(0).getRemarks());
        verify(goodsReceiptRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllGoodsReceipts returns empty list when no receipts exist")
    void getAllGoodsReceipts_WhenNoReceiptsExist_ReturnsEmptyList() {
        when(goodsReceiptRepository.findAll()).thenReturn(Collections.emptyList());

        List<GoodsReceipt> result = goodsReceiptService.getAllGoodsReceipts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(goodsReceiptRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getGoodsReceiptById returns receipt when receipt exists")
    void getGoodsReceiptById_WhenReceiptExists_ReturnsReceipt() {
        when(goodsReceiptRepository.findById(1)).thenReturn(Optional.of(sampleReceipt));

        Optional<GoodsReceipt> result = goodsReceiptService.getGoodsReceiptById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(5, result.get().getPurchaseOrderId());
        assertEquals(LocalDate.of(2026, 3, 10), result.get().getReceivedDate());
        verify(goodsReceiptRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getGoodsReceiptById returns empty Optional when receipt does not exist")
    void getGoodsReceiptById_WhenReceiptDoesNotExist_ReturnsEmpty() {
        when(goodsReceiptRepository.findById(999)).thenReturn(Optional.empty());

        Optional<GoodsReceipt> result = goodsReceiptService.getGoodsReceiptById(999);

        assertFalse(result.isPresent());
        verify(goodsReceiptRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("saveGoodsReceipt persists and returns saved goods receipt")
    void saveGoodsReceipt_Success_ReturnsSavedReceipt() {
        when(goodsReceiptRepository.save(sampleReceipt)).thenReturn(sampleReceipt);

        GoodsReceipt result = goodsReceiptService.saveGoodsReceipt(sampleReceipt);

        assertNotNull(result);
        assertEquals(5, result.getPurchaseOrderId());
        assertEquals("Received all items in good condition", result.getRemarks());
        verify(goodsReceiptRepository, times(1)).save(sampleReceipt);
    }
}
