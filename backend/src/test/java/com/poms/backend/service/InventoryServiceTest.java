package com.poms.backend.service;

import com.poms.backend.entity.Inventory;
import com.poms.backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleInventory = new Inventory();
        sampleInventory.setId(1);
        sampleInventory.setProductId(101);
        sampleInventory.setQuantityInStock(50);
        sampleInventory.setReorderLevel(10);
        sampleInventory.setLastUpdated(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllInventory returns inventory list when records exist")
    void getAllInventory_WhenInventoryExists_ReturnsList() {
        when(inventoryRepository.findAll()).thenReturn(List.of(sampleInventory));

        List<Inventory> result = inventoryService.getAllInventory();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101, result.get(0).getProductId());
        assertEquals(50, result.get(0).getQuantityInStock());
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllInventory returns empty list when no records exist")
    void getAllInventory_WhenNoInventoryExists_ReturnsEmptyList() {
        when(inventoryRepository.findAll()).thenReturn(Collections.emptyList());

        List<Inventory> result = inventoryService.getAllInventory();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getInventoryById returns inventory when record exists")
    void getInventoryById_WhenInventoryExists_ReturnsInventory() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(sampleInventory));

        Optional<Inventory> result = inventoryService.getInventoryById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(101, result.get().getProductId());
        assertEquals(50, result.get().getQuantityInStock());
        verify(inventoryRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getInventoryById returns empty Optional when record does not exist")
    void getInventoryById_WhenInventoryDoesNotExist_ReturnsEmpty() {
        when(inventoryRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Inventory> result = inventoryService.getInventoryById(999);

        assertFalse(result.isPresent());
        verify(inventoryRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("saveInventory persists and returns saved inventory")
    void saveInventory_Success_ReturnsSavedInventory() {
        when(inventoryRepository.save(sampleInventory)).thenReturn(sampleInventory);

        Inventory result = inventoryService.saveInventory(sampleInventory);

        assertNotNull(result);
        assertEquals(101, result.getProductId());
        assertEquals(50, result.getQuantityInStock());
        verify(inventoryRepository, times(1)).save(sampleInventory);
    }
}
