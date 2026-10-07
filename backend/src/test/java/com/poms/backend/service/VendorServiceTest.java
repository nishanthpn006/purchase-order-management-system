package com.poms.backend.service;

import com.poms.backend.dto.CreateVendorRequest;
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

    @Test
    @DisplayName("saveVendor persists and returns saved vendor")
    void saveVendor_Success_ReturnsSavedVendor() {
        when(vendorRepository.save(sampleVendor)).thenReturn(sampleVendor);

        Vendor result = vendorService.saveVendor(sampleVendor);

        assertNotNull(result);
        assertEquals("Acme Supplies Ltd", result.getVendorName());
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("createVendor successfully validates, creates, and persists vendor")
    void createVendor_Success_PersistsAndReturnsVendor() {
        CreateVendorRequest request = new CreateVendorRequest(
                "Global Tech Solutions",
                "Jane Doe",
                "jane@globaltech.com",
                "9123456780",
                "456 Silicon Valley, Bangalore",
                "29XYZAB1234C1Z9",
                "Active"
        );

        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> {
            Vendor v = invocation.getArgument(0);
            v.setId(10);
            return v;
        });

        Vendor created = vendorService.createVendor(request);

        assertNotNull(created);
        assertEquals(10, created.getId());
        assertEquals("Global Tech Solutions", created.getVendorName());
        assertEquals("Jane Doe", created.getContactPerson());
        assertEquals("jane@globaltech.com", created.getEmail());
        assertEquals("9123456780", created.getPhone());
        assertEquals("456 Silicon Valley, Bangalore", created.getAddress());
        assertEquals("29XYZAB1234C1Z9", created.getGstNumber());
        assertEquals("Active", created.getStatus());
        assertNotNull(created.getCreatedAt());

        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    @DisplayName("createVendor sets default status to Active when status is omitted")
    void createVendor_Success_WithDefaultStatusAndNullOptionalFields() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Minimal Supplies");

        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> {
            Vendor v = invocation.getArgument(0);
            v.setId(11);
            return v;
        });

        Vendor created = vendorService.createVendor(request);

        assertNotNull(created);
        assertEquals("Minimal Supplies", created.getVendorName());
        assertNull(created.getContactPerson());
        assertNull(created.getEmail());
        assertNull(created.getPhone());
        assertNull(created.getAddress());
        assertNull(created.getGstNumber());
        assertEquals("Active", created.getStatus());
        assertNotNull(created.getCreatedAt());

        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    @DisplayName("createVendor normalizes status to Inactive")
    void createVendor_Success_WithInactiveStatus() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Inactive Supplies");
        request.setStatus("inactive");

        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor created = vendorService.createVendor(request);

        assertEquals("Inactive", created.getStatus());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when request is null")
    void createVendor_NullRequest_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(null)
        );
        assertEquals("Request body cannot be null", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when vendor name is null or blank")
    void createVendor_BlankVendorName_ThrowsException() {
        CreateVendorRequest request1 = new CreateVendorRequest();
        request1.setVendorName(null);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request1)
        );
        assertEquals("Vendor name is required", ex1.getMessage());

        CreateVendorRequest request2 = new CreateVendorRequest();
        request2.setVendorName("   ");

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request2)
        );
        assertEquals("Vendor name is required", ex2.getMessage());

        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when vendor name exceeds 150 characters")
    void createVendor_VendorNameTooLong_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("A".repeat(151));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("Vendor name must not exceed 150 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when contact person exceeds 100 characters")
    void createVendor_ContactPersonTooLong_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Valid Name");
        request.setContactPerson("B".repeat(101));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("Contact person must not exceed 100 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when email format is invalid")
    void createVendor_InvalidEmailFormat_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Valid Name");
        request.setEmail("not-an-email");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("Invalid email format", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when email exceeds 100 characters")
    void createVendor_EmailTooLong_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Valid Name");
        request.setEmail("a".repeat(95) + "@example.com"); // > 100 chars

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("Email must not exceed 100 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when phone exceeds 20 characters")
    void createVendor_PhoneTooLong_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Valid Name");
        request.setPhone("123456789012345678901"); // 21 chars

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("Phone number must not exceed 20 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when GST number exceeds 30 characters")
    void createVendor_GstNumberTooLong_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Valid Name");
        request.setGstNumber("G".repeat(31));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("GST number must not exceed 30 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVendor throws IllegalArgumentException when status is invalid")
    void createVendor_InvalidStatus_ThrowsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("Valid Name");
        request.setStatus("Pending");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.createVendor(request)
        );
        assertEquals("Status must be either 'Active' or 'Inactive'", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }
}
