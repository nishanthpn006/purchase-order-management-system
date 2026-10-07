package com.poms.backend.service;

import com.poms.backend.dto.CreateProductRequest;
import com.poms.backend.dto.UpdateProductRequest;
import com.poms.backend.entity.Product;
import com.poms.backend.repository.ProductRepository;
import com.poms.backend.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    public ProductService(ProductRepository productRepository, VendorRepository vendorRepository) {
        this.productRepository = productRepository;
        this.vendorRepository = vendorRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product createProduct(CreateProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        String productName = request.getProductName().trim();
        if (productName.length() > 150) {
            throw new IllegalArgumentException("Product name must not exceed 150 characters");
        }

        if (request.getVendorId() == null) {
            throw new IllegalArgumentException("Vendor ID is required");
        }

        if (!vendorRepository.existsById(request.getVendorId())) {
            throw new IllegalArgumentException("Vendor not found with ID: " + request.getVendorId());
        }

        if (request.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unit price is required");
        }

        if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }

        Integer stockQuantity = 0;
        if (request.getStockQuantity() != null) {
            if (request.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative");
            }
            stockQuantity = request.getStockQuantity();
        }

        String category = null;
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            category = request.getCategory().trim();
            if (category.length() > 100) {
                throw new IllegalArgumentException("Category must not exceed 100 characters");
            }
        }

        String description = null;
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            description = request.getDescription().trim();
        }

        String unit = null;
        if (request.getUnit() != null && !request.getUnit().trim().isEmpty()) {
            unit = request.getUnit().trim();
            if (unit.length() > 30) {
                throw new IllegalArgumentException("Unit must not exceed 30 characters");
            }
        }

        String status = "Available";
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String rawStatus = request.getStatus().trim();
            if (!rawStatus.equalsIgnoreCase("Available") && !rawStatus.equalsIgnoreCase("Unavailable")) {
                throw new IllegalArgumentException("Status must be either 'Available' or 'Unavailable'");
            }
            status = rawStatus.equalsIgnoreCase("Available") ? "Available" : "Unavailable";
        }

        Product product = new Product();
        product.setVendorId(request.getVendorId());
        product.setProductName(productName);
        product.setCategory(category);
        product.setDescription(description);
        product.setUnitPrice(request.getUnitPrice());
        product.setStockQuantity(stockQuantity);
        product.setUnit(unit);
        product.setStatus(status);
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, UpdateProductRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        return updateProduct(id.intValue(), request);
    }

    public Product updateProduct(Integer id, UpdateProductRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + id));

        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        String productName = request.getProductName().trim();
        if (productName.length() > 150) {
            throw new IllegalArgumentException("Product name must not exceed 150 characters");
        }

        if (request.getVendorId() == null) {
            throw new IllegalArgumentException("Vendor ID is required");
        }

        if (!vendorRepository.existsById(request.getVendorId())) {
            throw new IllegalArgumentException("Vendor not found with ID: " + request.getVendorId());
        }

        if (request.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unit price is required");
        }

        if (request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }

        Integer stockQuantity = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        if (request.getStockQuantity() != null) {
            if (request.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative");
            }
            stockQuantity = request.getStockQuantity();
        }

        String category = null;
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            category = request.getCategory().trim();
            if (category.length() > 100) {
                throw new IllegalArgumentException("Category must not exceed 100 characters");
            }
        }

        String description = null;
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            description = request.getDescription().trim();
        }

        String unit = null;
        if (request.getUnit() != null && !request.getUnit().trim().isEmpty()) {
            unit = request.getUnit().trim();
            if (unit.length() > 30) {
                throw new IllegalArgumentException("Unit must not exceed 30 characters");
            }
        }

        String status = product.getStatus() != null ? product.getStatus() : "Available";
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String rawStatus = request.getStatus().trim();
            if (!rawStatus.equalsIgnoreCase("Available") && !rawStatus.equalsIgnoreCase("Unavailable")) {
                throw new IllegalArgumentException("Status must be either 'Available' or 'Unavailable'");
            }
            status = rawStatus.equalsIgnoreCase("Available") ? "Available" : "Unavailable";
        }

        product.setVendorId(request.getVendorId());
        product.setProductName(productName);
        product.setCategory(category);
        product.setDescription(description);
        product.setUnitPrice(request.getUnitPrice());
        product.setStockQuantity(stockQuantity);
        product.setUnit(unit);
        product.setStatus(status);

        return productRepository.save(product);
    }
}