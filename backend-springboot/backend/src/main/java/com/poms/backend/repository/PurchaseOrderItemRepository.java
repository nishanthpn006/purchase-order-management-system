package com.poms.backend.repository;

import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.PurchaseOrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, PurchaseOrderItemId> {
    List<PurchaseOrderItem> findByIdPurchaseOrderId(Integer purchaseOrderId);
}