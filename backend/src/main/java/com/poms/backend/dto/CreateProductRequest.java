package com.poms.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Request payload for creating a new product catalog item.
 * POST /api/products
 */
@Schema(description = "Product creation payload including vendor assignment and pricing")
public class CreateProductRequest {

    @Schema(description = "ID of the associated supplier vendor", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer vendorId;

    @Schema(description = "Name of the product", example = "Dell 24 Monitor", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @Schema(description = "Product category", example = "Monitor")
    private String category;

    @Schema(description = "Detailed product description", example = "24 inch Full HD monitor")
    private String description;

    @Schema(description = "Unit price per item", example = "12000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitPrice;

    @Schema(description = "Initial stock quantity (defaults to 0)", example = "20")
    private Integer stockQuantity;

    @Schema(description = "Unit of measurement", example = "Piece")
    private String unit;

    @Schema(description = "Availability status (defaults to Available)", example = "Available", allowableValues = {"Available", "Unavailable"})
    private String status;

    public CreateProductRequest() {
    }

    public CreateProductRequest(Integer vendorId, String productName, String category, String description, BigDecimal unitPrice, Integer stockQuantity, String unit, String status) {
        this.vendorId = vendorId;
        this.productName = productName;
        this.category = category;
        this.description = description;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.status = status;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
