package com.poms.backend.service;

import com.poms.backend.dto.CreateVendorRequest;
import com.poms.backend.dto.UpdateVendorRequest;
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
import java.util.NoSuchElementException;
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

    // ==========================================
    // updateVendor Tests
    // ==========================================

    @Test
    @DisplayName("updateVendor successfully updates all editable fields and persists vendor")
    void updateVendor_Success_AllFieldsUpdatedAndPersisted() {
        LocalDateTime originalCreatedAt = sampleVendor.getCreatedAt();
        UpdateVendorRequest request = new UpdateVendorRequest(
                "Updated Tech Solutions",
                "Jane Doe",
                "jane@updatedtech.com",
                "9876543211",
                "Electronic City, Bangalore",
                "29XYZAB5678C1Z2",
                "Inactive"
        );

        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor updated = vendorService.updateVendor(1, request);

        assertNotNull(updated);
        assertEquals(1, updated.getId(), "Vendor ID must remain unchanged");
        assertEquals(originalCreatedAt, updated.getCreatedAt(), "Vendor created_at must remain unchanged");
        assertEquals("Updated Tech Solutions", updated.getVendorName());
        assertEquals("Jane Doe", updated.getContactPerson());
        assertEquals("jane@updatedtech.com", updated.getEmail());
        assertEquals("9876543211", updated.getPhone());
        assertEquals("Electronic City, Bangalore", updated.getAddress());
        assertEquals("29XYZAB5678C1Z2", updated.getGstNumber());
        assertEquals("Inactive", updated.getStatus());

        verify(vendorRepository, times(1)).findById(1);
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("updateVendor with Long ID successfully updates vendor")
    void updateVendor_Success_WithLongId() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Long ID Solutions");

        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor updated = vendorService.updateVendor(1L, request);

        assertNotNull(updated);
        assertEquals("Long ID Solutions", updated.getVendorName());
        verify(vendorRepository, times(1)).findById(1);
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("updateVendor normalizes status to Active or Inactive case-insensitively")
    void updateVendor_Success_StatusNormalization() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVendorRequest reqActive = new UpdateVendorRequest();
        reqActive.setVendorName("Acme Supplies");
        reqActive.setStatus("active");
        Vendor updated1 = vendorService.updateVendor(1, reqActive);
        assertEquals("Active", updated1.getStatus());

        UpdateVendorRequest reqInactive = new UpdateVendorRequest();
        reqInactive.setVendorName("Acme Supplies");
        reqInactive.setStatus("INACTIVE");
        Vendor updated2 = vendorService.updateVendor(1, reqInactive);
        assertEquals("Inactive", updated2.getStatus());

        verify(vendorRepository, times(2)).save(sampleVendor);
    }

    @Test
    @DisplayName("updateVendor retains existing status when status is omitted")
    void updateVendor_Success_RetainsExistingStatusWhenNull() {
        sampleVendor.setStatus("Inactive");
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Acme Supplies");
        request.setStatus(null);

        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor updated = vendorService.updateVendor(1, request);

        assertEquals("Inactive", updated.getStatus(), "Should retain existing Inactive status");
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("updateVendor allows setting optional fields to null")
    void updateVendor_Success_ClearsOptionalFieldsWhenNull() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Bare Vendor");
        request.setContactPerson(null);
        request.setEmail(null);
        request.setPhone(null);
        request.setAddress(null);
        request.setGstNumber(null);
        request.setStatus("Active");

        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor updated = vendorService.updateVendor(1, request);

        assertNotNull(updated);
        assertEquals("Bare Vendor", updated.getVendorName());
        assertNull(updated.getContactPerson());
        assertNull(updated.getEmail());
        assertNull(updated.getPhone());
        assertNull(updated.getAddress());
        assertNull(updated.getGstNumber());
        assertEquals("Active", updated.getStatus());
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("updateVendor throws NoSuchElementException when vendor does not exist")
    void updateVendor_VendorNotFound_ThrowsException() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Nonexistent Vendor");

        when(vendorRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                vendorService.updateVendor(999, request)
        );
        assertEquals("Vendor not found with ID: 999", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when ID is null")
    void updateVendor_NullId_ThrowsException() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor((Integer) null, request)
        );
        assertEquals("Vendor ID is required", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor((Long) null, request)
        );
        assertEquals("Vendor ID is required", ex2.getMessage());

        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when request body is null")
    void updateVendor_NullRequest_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, null)
        );
        assertEquals("Request body cannot be null", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when vendor name is null or blank")
    void updateVendor_BlankVendorName_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest reqNull = new UpdateVendorRequest();
        reqNull.setVendorName(null);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, reqNull)
        );
        assertEquals("Vendor name is required", ex1.getMessage());

        UpdateVendorRequest reqBlank = new UpdateVendorRequest();
        reqBlank.setVendorName("   ");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, reqBlank)
        );
        assertEquals("Vendor name is required", ex2.getMessage());

        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when vendor name exceeds 150 characters")
    void updateVendor_VendorNameTooLong_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("V".repeat(151));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("Vendor name must not exceed 150 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when contact person exceeds 100 characters")
    void updateVendor_ContactPersonTooLong_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");
        request.setContactPerson("C".repeat(101));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("Contact person must not exceed 100 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when email format is invalid")
    void updateVendor_InvalidEmailFormat_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");
        request.setEmail("bad-email-address");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("Invalid email format", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when email exceeds 100 characters")
    void updateVendor_EmailTooLong_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");
        request.setEmail("e".repeat(95) + "@domain.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("Email must not exceed 100 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when phone exceeds 20 characters")
    void updateVendor_PhoneTooLong_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");
        request.setPhone("1".repeat(21));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("Phone number must not exceed 20 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when GST number exceeds 30 characters")
    void updateVendor_GstNumberTooLong_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");
        request.setGstNumber("G".repeat(31));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("GST number must not exceed 30 characters", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateVendor throws IllegalArgumentException when status is invalid")
    void updateVendor_InvalidStatus_ThrowsException() {
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Valid Name");
        request.setStatus("Archived");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                vendorService.updateVendor(1, request)
        );
        assertEquals("Status must be either 'Active' or 'Inactive'", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    // ==========================================
    // deactivateVendor Tests
    // ==========================================

    @Test
    @DisplayName("deactivateVendor sets vendor status to Inactive and saves")
    void deactivateVendor_Success_VendorBecomesInactive() {
        sampleVendor.setStatus("Active");
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor result = vendorService.deactivateVendor(1);

        assertNotNull(result);
        assertEquals("Inactive", result.getStatus());
        verify(vendorRepository, times(1)).findById(1);
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("deactivateVendor with Long ID sets vendor status to Inactive")
    void deactivateVendor_Success_WithLongId() {
        sampleVendor.setStatus("Active");
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor result = vendorService.deactivateVendor(1L);

        assertNotNull(result);
        assertEquals("Inactive", result.getStatus());
        verify(vendorRepository, times(1)).findById(1);
        verify(vendorRepository, times(1)).save(sampleVendor);
    }

    @Test
    @DisplayName("deactivateVendor is idempotent: already Inactive remains Inactive without error")
    void deactivateVendor_AlreadyInactive_RemainsInactive() {
        sampleVendor.setStatus("Inactive");
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));

        Vendor result = vendorService.deactivateVendor(1);

        assertNotNull(result);
        assertEquals("Inactive", result.getStatus());
        verify(vendorRepository, times(1)).findById(1);
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivateVendor throws NoSuchElementException when vendor does not exist")
    void deactivateVendor_VendorNotFound_ThrowsException() {
        when(vendorRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                vendorService.deactivateVendor(999)
        );
        assertEquals("Vendor not found with ID: 999", ex.getMessage());
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivateVendor preserves all other vendor fields intact")
    void deactivateVendor_PreservesExistingFields() {
        LocalDateTime originalCreatedAt = sampleVendor.getCreatedAt();
        sampleVendor.setStatus("Active");
        when(vendorRepository.findById(1)).thenReturn(Optional.of(sampleVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor result = vendorService.deactivateVendor(1);

        assertNotNull(result);
        assertEquals(1, result.getId(), "Vendor ID must remain unchanged");
        assertEquals("Acme Supplies Ltd", result.getVendorName());
        assertEquals("Alice Smith", result.getContactPerson());
        assertEquals("alice@acme.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());
        assertEquals("123 Industrial Way, Tech Park", result.getAddress());
        assertEquals("29ABCDE1234F1Z5", result.getGstNumber());
        assertEquals(originalCreatedAt, result.getCreatedAt(), "Vendor created_at must remain unchanged");
        assertEquals("Inactive", result.getStatus());
    }

    @Test
    @DisplayName("deactivateVendor throws IllegalArgumentException when ID is null")
    void deactivateVendor_NullId_ThrowsException() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.deactivateVendor((Integer) null)
        );
        assertEquals("Vendor ID is required", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                vendorService.deactivateVendor((Long) null)
        );
        assertEquals("Vendor ID is required", ex2.getMessage());
        verify(vendorRepository, never()).save(any());
    }
}
