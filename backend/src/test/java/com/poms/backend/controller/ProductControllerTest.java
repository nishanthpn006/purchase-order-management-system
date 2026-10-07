package com.poms.backend.controller;

import com.poms.backend.dto.CreateProductRequest;
import com.poms.backend.entity.Product;
import com.poms.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1);
        sampleProduct.setVendorId(1);
        sampleProduct.setProductName("Dell 24 Monitor");
        sampleProduct.setCategory("Monitor");
        sampleProduct.setDescription("24 inch Full HD monitor");
        sampleProduct.setUnitPrice(new BigDecimal("12000.00"));
        sampleProduct.setStockQuantity(20);
        sampleProduct.setUnit("Piece");
        sampleProduct.setStatus("Available");
        sampleProduct.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllProducts returns HTTP 200 with list of products")
    void getAllProducts_ReturnsOk() {
        when(productService.getAllProducts()).thenReturn(List.of(sampleProduct));

        ResponseEntity<List<Product>> response = productController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Dell 24 Monitor", response.getBody().get(0).getProductName());
    }

    @Test
    @DisplayName("getProductById returns HTTP 200 when product exists")
    void getProductById_WhenFound_ReturnsOk() {
        when(productService.getProductById(1)).thenReturn(Optional.of(sampleProduct));

        ResponseEntity<?> response = productController.getProductById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Product);
        Product body = (Product) response.getBody();
        assertEquals("Dell 24 Monitor", body.getProductName());
    }

    @Test
    @DisplayName("getProductById returns HTTP 404 when product does not exist")
    void getProductById_WhenNotFound_Returns404() {
        when(productService.getProductById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = productController.getProductById(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Product not found", body.get("error"));
    }

    @Test
    @DisplayName("createProduct returns HTTP 201 Created on valid request")
    void createProduct_ValidRequest_ReturnsCreated() {
        CreateProductRequest request = new CreateProductRequest(
                1,
                "Dell 24 Monitor",
                "Monitor",
                "24 inch Full HD monitor",
                new BigDecimal("12000.00"),
                20,
                "Piece",
                "Available"
        );

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(sampleProduct);

        ResponseEntity<?> response = productController.createProduct(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Product);
        Product body = (Product) response.getBody();
        assertEquals("Dell 24 Monitor", body.getProductName());
        verify(productService, times(1)).createProduct(request);
    }

    @Test
    @DisplayName("createProduct returns HTTP 400 Bad Request when validation fails in service")
    void createProduct_ValidationError_ReturnsBadRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("");

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new IllegalArgumentException("Product name is required"));

        ResponseEntity<?> response = productController.createProduct(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Product name is required", body.get("error"));
    }

    @Test
    @DisplayName("createProduct returns HTTP 400 Bad Request when vendor is not found")
    void createProduct_VendorNotFound_ReturnsBadRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(999);

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new IllegalArgumentException("Vendor not found with ID: 999"));

        ResponseEntity<?> response = productController.createProduct(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Vendor not found with ID: 999", body.get("error"));
    }

    @Test
    @DisplayName("createProduct returns HTTP 400 Bad Request when request body is null")
    void createProduct_NullRequest_ReturnsBadRequest() {
        ResponseEntity<?> response = productController.createProduct(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Request body is required", body.get("error"));
        verify(productService, never()).createProduct(any());
    }
}
