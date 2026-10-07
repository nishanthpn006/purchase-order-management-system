package com.poms.backend.service;

import com.poms.backend.dto.CreateProductRequest;
import com.poms.backend.dto.UpdateProductRequest;
import com.poms.backend.entity.Product;
import com.poms.backend.repository.ProductRepository;
import com.poms.backend.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1);
        sampleProduct.setVendorId(10);
        sampleProduct.setProductName("Ergonomic Office Chair");
        sampleProduct.setCategory("Furniture");
        sampleProduct.setDescription("High-grade mesh ergonomic chair with lumbar support");
        sampleProduct.setUnitPrice(new BigDecimal("7499.50"));
        sampleProduct.setStockQuantity(25);
        sampleProduct.setUnit("Pieces");
        sampleProduct.setStatus("Available");
        sampleProduct.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllProducts returns product list when products exist")
    void getAllProducts_WhenProductsExist_ReturnsProductList() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ergonomic Office Chair", result.get(0).getProductName());
        assertEquals("Furniture", result.get(0).getCategory());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllProducts returns empty list when no products exist")
    void getAllProducts_WhenNoProductsExist_ReturnsEmptyList() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getProductById returns product when product exists")
    void getProductById_WhenProductExists_ReturnsProduct() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        Optional<Product> result = productService.getProductById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Ergonomic Office Chair", result.get().getProductName());
        assertEquals(new BigDecimal("7499.50"), result.get().getUnitPrice());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getProductById returns empty Optional when product does not exist")
    void getProductById_WhenProductDoesNotExist_ReturnsEmpty() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("saveProduct persists and returns saved product")
    void saveProduct_Success_ReturnsSavedProduct() {
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product result = productService.saveProduct(sampleProduct);

        assertNotNull(result);
        assertEquals("Ergonomic Office Chair", result.getProductName());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("createProduct successfully validates, links existing vendor, and persists product")
    void createProduct_Success_PersistsAndReturnsProduct() {
        CreateProductRequest request = new CreateProductRequest(
                10,
                "Dell 24 Monitor",
                "Monitor",
                "24 inch Full HD monitor",
                new BigDecimal("12000.00"),
                20,
                "Piece",
                "Available"
        );

        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(100);
            return p;
        });

        Product created = productService.createProduct(request);

        assertNotNull(created);
        assertEquals(100, created.getId());
        assertEquals(10, created.getVendorId());
        assertEquals("Dell 24 Monitor", created.getProductName());
        assertEquals("Monitor", created.getCategory());
        assertEquals("24 inch Full HD monitor", created.getDescription());
        assertEquals(new BigDecimal("12000.00"), created.getUnitPrice());
        assertEquals(20, created.getStockQuantity());
        assertEquals("Piece", created.getUnit());
        assertEquals("Available", created.getStatus());
        assertNotNull(created.getCreatedAt());

        verify(vendorRepository, times(1)).existsById(10);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct defaults stock quantity to 0 and status to Available when omitted")
    void createProduct_Success_WithDefaultStatusAndStock() {
        CreateProductRequest request = new CreateProductRequest();
        request.setVendorId(2);
        request.setProductName("Basic Cable");
        request.setUnitPrice(new BigDecimal("150.00"));

        when(vendorRepository.existsById(2)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(101);
            return p;
        });

        Product created = productService.createProduct(request);

        assertNotNull(created);
        assertEquals("Basic Cable", created.getProductName());
        assertEquals(0, created.getStockQuantity());
        assertEquals("Available", created.getStatus());
        assertNull(created.getCategory());
        assertNull(created.getDescription());
        assertNull(created.getUnit());
        assertNotNull(created.getCreatedAt());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct normalizes status to Unavailable")
    void createProduct_Success_WithUnavailableStatus() {
        CreateProductRequest request = new CreateProductRequest();
        request.setVendorId(1);
        request.setProductName("Discontinued Part");
        request.setUnitPrice(new BigDecimal("500.00"));
        request.setStatus("unavailable");

        when(vendorRepository.existsById(1)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.createProduct(request);

        assertEquals("Unavailable", created.getStatus());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when request is null")
    void createProduct_NullRequest_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(null)
        );
        assertEquals("Request body cannot be null", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when product name is null or blank")
    void createProduct_MissingProductName_ThrowsException() {
        CreateProductRequest request1 = new CreateProductRequest();
        request1.setProductName(null);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request1)
        );
        assertEquals("Product name is required", ex1.getMessage());

        CreateProductRequest request2 = new CreateProductRequest();
        request2.setProductName("   ");

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request2)
        );
        assertEquals("Product name is required", ex2.getMessage());

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when product name exceeds 150 characters")
    void createProduct_ProductNameTooLong_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("P".repeat(151));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Product name must not exceed 150 characters", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when vendor ID is null")
    void createProduct_MissingVendorId_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Vendor ID is required", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when vendor does not exist")
    void createProduct_VendorNotFound_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(999);

        when(vendorRepository.existsById(999)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Vendor not found with ID: 999", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when unit price is null")
    void createProduct_MissingUnitPrice_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(1);
        request.setUnitPrice(null);

        when(vendorRepository.existsById(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Unit price is required", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when unit price is negative")
    void createProduct_NegativeUnitPrice_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(1);
        request.setUnitPrice(new BigDecimal("-1.00"));

        when(vendorRepository.existsById(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Unit price cannot be negative", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when stock quantity is negative")
    void createProduct_NegativeStockQuantity_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(1);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setStockQuantity(-5);

        when(vendorRepository.existsById(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Stock quantity cannot be negative", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when category exceeds 100 characters")
    void createProduct_CategoryTooLong_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(1);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setCategory("C".repeat(101));

        when(vendorRepository.existsById(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Category must not exceed 100 characters", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when unit exceeds 30 characters")
    void createProduct_UnitTooLong_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(1);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setUnit("U".repeat(31));

        when(vendorRepository.existsById(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Unit must not exceed 30 characters", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct throws IllegalArgumentException when status is invalid")
    void createProduct_InvalidStatus_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("Valid Product");
        request.setVendorId(1);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setStatus("Archived");

        when(vendorRepository.existsById(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Status must be either 'Available' or 'Unavailable'", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    // ==========================================
    // updateProduct Tests
    // ==========================================

    @Test
    @DisplayName("updateProduct successfully updates all fields, preserving ID and createdAt")
    void updateProduct_Success_UpdatesAllFields() {
        LocalDateTime originalCreatedAt = sampleProduct.getCreatedAt();
        UpdateProductRequest request = new UpdateProductRequest(
                20,
                "Updated Chair Deluxe",
                "Seating",
                "Deluxe high-back mesh chair",
                new BigDecimal("8999.00"),
                50,
                "Units",
                "Unavailable"
        );

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(20)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(1, request);

        assertNotNull(updated);
        assertEquals(1, updated.getId());
        assertEquals(originalCreatedAt, updated.getCreatedAt());
        assertEquals(20, updated.getVendorId());
        assertEquals("Updated Chair Deluxe", updated.getProductName());
        assertEquals("Seating", updated.getCategory());
        assertEquals("Deluxe high-back mesh chair", updated.getDescription());
        assertEquals(new BigDecimal("8999.00"), updated.getUnitPrice());
        assertEquals(50, updated.getStockQuantity());
        assertEquals("Units", updated.getUnit());
        assertEquals("Unavailable", updated.getStatus());

        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("updateProduct with Long ID successfully updates product")
    void updateProduct_Success_WithLongId() {
        UpdateProductRequest request = new UpdateProductRequest(
                20,
                "Updated Chair",
                "Furniture",
                "Desc",
                new BigDecimal("5000.00"),
                10,
                "Piece",
                "Available"
        );

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(20)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(1L, request);

        assertNotNull(updated);
        assertEquals("Updated Chair", updated.getProductName());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("updateProduct normalizes status case to 'Available' or 'Unavailable'")
    void updateProduct_Success_NormalizesStatusCase() {
        UpdateProductRequest request = new UpdateProductRequest(
                10,
                "Case Normalization Test",
                "Furniture",
                "Desc",
                new BigDecimal("100.00"),
                5,
                "Piece",
                "unavailable"
        );

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(1, request);

        assertEquals("Unavailable", updated.getStatus());
    }

    @Test
    @DisplayName("updateProduct throws NoSuchElementException when product is not found")
    void updateProduct_ProductNotFound_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest(
                10,
                "Non-existent Product",
                "Furniture",
                "Desc",
                new BigDecimal("100.00"),
                5,
                "Piece",
                "Available"
        );

        when(productRepository.findById(999)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                productService.updateProduct(999, request)
        );
        assertEquals("Product not found with ID: 999", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when ID is null")
    void updateProduct_NullId_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct((Integer) null, request)
        );
        assertEquals("Product ID is required", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct((Long) null, request)
        );
        assertEquals("Product ID is required", ex2.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when request body is null")
    void updateProduct_NullRequest_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, null)
        );
        assertEquals("Request body cannot be null", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when product name is null or blank")
    void updateProduct_BlankProductName_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("   ");

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Product name is required", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when product name exceeds 150 chars")
    void updateProduct_ProductNameTooLong_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("A".repeat(151));

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Product name must not exceed 150 characters", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when vendor ID is null")
    void updateProduct_NullVendorId_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(null);

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Vendor ID is required", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when vendor does not exist")
    void updateProduct_VendorNotFound_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(999);

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(999)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Vendor not found with ID: 999", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when unit price is null")
    void updateProduct_NullUnitPrice_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(10);
        request.setUnitPrice(null);

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Unit price is required", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when unit price is negative")
    void updateProduct_NegativeUnitPrice_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(10);
        request.setUnitPrice(new BigDecimal("-1.00"));

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Unit price cannot be negative", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when stock quantity is negative")
    void updateProduct_NegativeStockQuantity_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(10);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setStockQuantity(-10);

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Stock quantity cannot be negative", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when category exceeds 100 chars")
    void updateProduct_CategoryTooLong_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(10);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setCategory("C".repeat(101));

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Category must not exceed 100 characters", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when unit exceeds 30 chars")
    void updateProduct_UnitTooLong_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(10);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setUnit("U".repeat(31));

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Unit must not exceed 30 characters", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct throws IllegalArgumentException when status is invalid")
    void updateProduct_InvalidStatus_ThrowsException() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setProductName("Valid Name");
        request.setVendorId(10);
        request.setUnitPrice(new BigDecimal("100.00"));
        request.setStatus("Archived");

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(vendorRepository.existsById(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productService.updateProduct(1, request)
        );
        assertEquals("Status must be either 'Available' or 'Unavailable'", ex.getMessage());
        verify(productRepository, never()).save(any());
    }
}
