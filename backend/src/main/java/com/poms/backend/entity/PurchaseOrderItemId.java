package com.poms.backend.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PurchaseOrderItemId implements Serializable {

    private Integer purchaseOrderId;

    private Integer productId;

    public PurchaseOrderItemId() {
    }

    public PurchaseOrderItemId(Integer purchaseOrderId, Integer productId) {
        this.purchaseOrderId = purchaseOrderId;
        this.productId = productId;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PurchaseOrderItemId)) return false;
        PurchaseOrderItemId that = (PurchaseOrderItemId) o;
        return Objects.equals(purchaseOrderId, that.purchaseOrderId)
                && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchaseOrderId, productId);
    }
}