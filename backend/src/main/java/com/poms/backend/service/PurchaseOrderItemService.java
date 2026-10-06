package com.poms.backend.service;

import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.entity.PurchaseOrderItemId;
import com.poms.backend.repository.PurchaseOrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderItemService {

    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    public PurchaseOrderItemService(PurchaseOrderItemRepository purchaseOrderItemRepository) {
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
    }

    public List<PurchaseOrderItem> getAllPurchaseOrderItems() {
        return purchaseOrderItemRepository.findAll();
    }

    public Optional<PurchaseOrderItem> getPurchaseOrderItemById(PurchaseOrderItemId id) {
        return purchaseOrderItemRepository.findById(id);
    }

    public List<PurchaseOrderItem> getItemsByPurchaseOrderId(Integer purchaseOrderId) {
        return purchaseOrderItemRepository.findByIdPurchaseOrderId(purchaseOrderId);
    }

    public PurchaseOrderItem savePurchaseOrderItem(PurchaseOrderItem item) {
        return purchaseOrderItemRepository.save(item);
    }
}