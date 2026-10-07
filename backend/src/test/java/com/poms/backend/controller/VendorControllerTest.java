package com.poms.backend.controller;

import com.poms.backend.dto.CreateVendorRequest;
import com.poms.backend.dto.UpdateVendorRequest;
import com.poms.backend.entity.Vendor;
import com.poms.backend.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private VendorController vendorController;

    private Vendor sampleVendor;

    @BeforeEach
    void setUp() {
        sampleVendor = new Vendor();
        sampleVendor.setId(1);
        sampleVendor.setVendorName("Dell Technologies");
        sampleVendor.setContactPerson("Arun Patel");
        sampleVendor.setEmail("dell@poms.com");
        sampleVendor.setPhone("9876543210");
        sampleVendor.setAddress("Bangalore");
        sampleVendor.setGstNumber("29ABCDE1234F1Z5");
        sampleVendor.setStatus("Active");
        sampleVendor.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllVendors returns HTTP 200 with list of vendors")
    void getAllVendors_ReturnsOk() {
        when(vendorService.getAllVendors()).thenReturn(List.of(sampleVendor));

        ResponseEntity<List<Vendor>> response = vendorController.getAllVendors();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Dell Technologies", response.getBody().get(0).getVendorName());
    }

    @Test
    @DisplayName("getVendorById returns HTTP 200 when vendor exists")
    void getVendorById_WhenFound_ReturnsOk() {
        when(vendorService.getVendorById(1)).thenReturn(Optional.of(sampleVendor));

        ResponseEntity<?> response = vendorController.getVendorById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Vendor);
        Vendor body = (Vendor) response.getBody();
        assertEquals("Dell Technologies", body.getVendorName());
    }

    @Test
    @DisplayName("getVendorById returns HTTP 404 when vendor does not exist")
    void getVendorById_WhenNotFound_Returns404() {
        when(vendorService.getVendorById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = vendorController.getVendorById(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Vendor not found", body.get("error"));
    }

    @Test
    @DisplayName("createVendor returns HTTP 201 Created on valid request")
    void createVendor_ValidRequest_ReturnsCreated() {
        CreateVendorRequest request = new CreateVendorRequest(
                "Dell Technologies",
                "Arun Patel",
                "dell@poms.com",
                "9876543210",
                "Bangalore",
                "29ABCDE1234F1Z5",
                "Active"
        );

        when(vendorService.createVendor(any(CreateVendorRequest.class))).thenReturn(sampleVendor);

        ResponseEntity<?> response = vendorController.createVendor(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Vendor);
        Vendor body = (Vendor) response.getBody();
        assertEquals("Dell Technologies", body.getVendorName());
        verify(vendorService, times(1)).createVendor(request);
    }

    @Test
    @DisplayName("createVendor returns HTTP 400 Bad Request when validation fails in service")
    void createVendor_ValidationError_ReturnsBadRequest() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setVendorName("");

        when(vendorService.createVendor(any(CreateVendorRequest.class)))
                .thenThrow(new IllegalArgumentException("Vendor name is required"));

        ResponseEntity<?> response = vendorController.createVendor(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Vendor name is required", body.get("error"));
    }

    @Test
    @DisplayName("createVendor returns HTTP 400 Bad Request when request body is null")
    void createVendor_NullRequest_ReturnsBadRequest() {
        ResponseEntity<?> response = vendorController.createVendor(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Request body is required", body.get("error"));
        verify(vendorService, never()).createVendor(any());
    }

    // ==========================================
    // updateVendor Tests
    // ==========================================

    @Test
    @DisplayName("updateVendor returns HTTP 200 OK on valid request")
    void updateVendor_ValidRequest_ReturnsOk() {
        UpdateVendorRequest request = new UpdateVendorRequest(
                "Dell Technologies Inc",
                "Arun Patel",
                "dell@poms.com",
                "9876543210",
                "Bangalore",
                "29ABCDE1234F1Z5",
                "Active"
        );

        Vendor updated = new Vendor();
        updated.setId(1);
        updated.setVendorName("Dell Technologies Inc");
        updated.setStatus("Active");

        when(vendorService.updateVendor(eq(1), any(UpdateVendorRequest.class))).thenReturn(updated);

        ResponseEntity<?> response = vendorController.updateVendor(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Vendor);
        Vendor body = (Vendor) response.getBody();
        assertEquals("Dell Technologies Inc", body.getVendorName());
        verify(vendorService, times(1)).updateVendor(1, request);
    }

    @Test
    @DisplayName("updateVendor returns HTTP 404 Not Found when vendor does not exist")
    void updateVendor_VendorNotFound_Returns404() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("Dell Technologies Inc");

        when(vendorService.updateVendor(eq(999), any(UpdateVendorRequest.class)))
                .thenThrow(new NoSuchElementException("Vendor not found with ID: 999"));

        ResponseEntity<?> response = vendorController.updateVendor(999, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Vendor not found with ID: 999", body.get("error"));
        verify(vendorService, times(1)).updateVendor(999, request);
    }

    @Test
    @DisplayName("updateVendor returns HTTP 400 Bad Request when validation fails in service")
    void updateVendor_ValidationError_ReturnsBadRequest() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setVendorName("");

        when(vendorService.updateVendor(eq(1), any(UpdateVendorRequest.class)))
                .thenThrow(new IllegalArgumentException("Vendor name is required"));

        ResponseEntity<?> response = vendorController.updateVendor(1, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Vendor name is required", body.get("error"));
    }

    @Test
    @DisplayName("updateVendor returns HTTP 400 Bad Request when ID is null")
    void updateVendor_NullId_ReturnsBadRequest() {
        UpdateVendorRequest request = new UpdateVendorRequest();

        ResponseEntity<?> response = vendorController.updateVendor(null, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Vendor ID is required", body.get("error"));
        verify(vendorService, never()).updateVendor(any(Integer.class), any());
    }

    @Test
    @DisplayName("updateVendor returns HTTP 400 Bad Request when request body is null")
    void updateVendor_NullRequest_ReturnsBadRequest() {
        ResponseEntity<?> response = vendorController.updateVendor(1, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Request body is required", body.get("error"));
        verify(vendorService, never()).updateVendor(any(Integer.class), any());
    }
}
