package com.poms.backend.service;

import com.poms.backend.entity.Vendor;
import com.poms.backend.repository.VendorRepository;
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
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorService vendorService;

    private Vendor sampleVendor;

    @BeforeEach
    void setUp() {
        sampleVendor = new Vendor();
        sampleVendor.setId(1);
        sampleVendor.setVendorName("Acme Supplies Ltd");
        sampleVendor.setContactPerson("Alice Smith");
        sampleVendor.setEmail("alice@acme.com");
        sampleVendor.setPhone("9876543210");
        sampleVendor.setAddress("123 Industrial Way, Tech Park");
        sampleVendor.setGstNumber("29ABCDE1234F1Z5");
        sampleVendor.setStatus("Active");
        sampleVendor.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllVendors returns list of vendors when vendors exist")
    void getAllVendors_WhenVendorsExist_ReturnsVendorList() {
        when(vendorRepository.findAll()).thenReturn(List.of(sampleVendor));

        List<Vendor> result = vendorService.getAllVendors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Acme Supplies Ltd", result.get(0).getVendorName());
        verify(vendorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllVendors returns empty list when no vendors exist")
    void getAllVendors_WhenNoVendorsExist_ReturnsEmptyList() {
        when(vendorRepository.findAll()).thenReturn(Collections.emptyList());

        List<Vendor> result = vendorService.getAllVendors();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(vendorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getVendorById returns vendor when vendor exists")
    void getVendorById_WhenVendorExists_ReturnsVendor() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        Optional<Vendor> result = vendorService.getVendorById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Acme Supplies Ltd", result.get().getVendorName());
        assertEquals("alice@acme.com", result.get().getEmail());
        verify(vendorRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getVendorById returns empty Optional when vendor does not exist")
    void getVendorById_WhenVendorDoesNotExist_ReturnsEmpty() {
        when(vendorRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Vendor> result = vendorService.getVendorById(999);

        assertFalse(result.isPresent());
        verify(vendorRepository, times(1)).findById(999);
    }
}
