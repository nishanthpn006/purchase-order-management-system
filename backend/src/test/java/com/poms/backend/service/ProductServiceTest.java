package com.poms.backend.service;

import com.poms.backend.entity.Product;
import com.poms.backend.repository.ProductRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

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
}
