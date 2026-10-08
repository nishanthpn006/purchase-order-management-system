package com.poms.backend.service;

import com.poms.backend.dto.CreateGoodsReceiptRequest;
import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.entity.GoodsReceiptItem;
import com.poms.backend.entity.Inventory;
import com.poms.backend.entity.Product;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.PurchaseOrderItem;
import com.poms.backend.repository.GoodsReceiptItemRepository;
import com.poms.backend.repository.GoodsReceiptRepository;
import com.poms.backend.repository.InventoryRepository;
import com.poms.backend.repository.ProductRepository;
import com.poms.backend.repository.PurchaseOrderItemRepository;
import com.poms.backend.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GoodsReceiptService {

    private static final int DEFAULT_REORDER_LEVEL = 10;

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public GoodsReceiptService(GoodsReceiptRepository goodsReceiptRepository,
                               GoodsReceiptItemRepository goodsReceiptItemRepository,
                               PurchaseOrderRepository purchaseOrderRepository,
                               PurchaseOrderItemRepository purchaseOrderItemRepository,
                               InventoryRepository inventoryRepository,
                               ProductRepository productRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptItemRepository = goodsReceiptItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    public List<GoodsReceipt> getAllGoodsReceipts() {
        return goodsReceiptRepository.findAll();
    }

    public Optional<GoodsReceipt> getGoodsReceiptById(Integer id) {
        return goodsReceiptRepository.findById(id);
    }

    public GoodsReceipt saveGoodsReceipt(GoodsReceipt goodsReceipt) {
        return goodsReceiptRepository.save(goodsReceipt);
    }

    /**
     * Atomically creates a Goods Receipt against an Approved Purchase Order,
     * persists header and line items, updates inventory and product stock,
     * prevents over-receiving, and transitions PO status to Completed if fully fulfilled.
     */
    @Transactional
    public GoodsReceipt createGoodsReceipt(CreateGoodsReceiptRequest request, Integer receivedByUserId) {
        // A. Validate request
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.getPurchaseOrderId() == null) {
            throw new IllegalArgumentException("Purchase order ID is required");
        }
        if (request.getReceivedDate() == null) {
            throw new IllegalArgumentException("Received date is required");
        }
        if (receivedByUserId == null) {
            throw new IllegalArgumentException("Received by user ID is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Goods receipt must contain at least one line item");
        }

        Set<Integer> submittedProductIds = new HashSet<>();
        for (CreateGoodsReceiptRequest.ReceiptItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for all receipt items");
            }
            if (itemReq.getReceivedQuantity() == null || itemReq.getReceivedQuantity() <= 0) {
                throw new IllegalArgumentException("Received quantity must be greater than zero for product ID: " + itemReq.getProductId());
            }
            if (!submittedProductIds.add(itemReq.getProductId())) {
                throw new IllegalArgumentException("Duplicate product ID found in receipt items: " + itemReq.getProductId());
            }
        }

        // B. Validate Purchase Order
        PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase order with ID " + request.getPurchaseOrderId() + " does not exist"));

        if (!"Approved".equalsIgnoreCase(po.getStatus())) {
            throw new IllegalArgumentException("Goods receipt is allowed only for Approved purchase orders");
        }

        // C. Validate PO line membership
        List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByIdPurchaseOrderId(po.getId());
        if (poItems == null || poItems.isEmpty()) {
            throw new IllegalArgumentException("Purchase order with ID " + po.getId() + " has no line items");
        }

        Map<Integer, Integer> orderedQuantities = new HashMap<>();
        for (PurchaseOrderItem poItem : poItems) {
            orderedQuantities.put(poItem.getId().getProductId(), poItem.getQuantity());
        }

        for (CreateGoodsReceiptRequest.ReceiptItemRequest itemReq : request.getItems()) {
            if (!orderedQuantities.containsKey(itemReq.getProductId())) {
                throw new IllegalArgumentException("Product ID " + itemReq.getProductId() + " is not part of purchase order " + po.getId());
            }
        }

        // D. Calculate previously received quantities
        List<Object[]> previousReceivedList = goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(po.getId());
        Map<Integer, Integer> alreadyReceivedMap = new HashMap<>();
        if (previousReceivedList != null) {
            for (Object[] row : previousReceivedList) {
                Integer prodId = (Integer) row[0];
                Number sumQty = (Number) row[1];
                alreadyReceivedMap.put(prodId, sumQty != null ? sumQty.intValue() : 0);
            }
        }

        // E. Prevent over-receiving
        for (CreateGoodsReceiptRequest.ReceiptItemRequest itemReq : request.getItems()) {
            int orderedQty = orderedQuantities.get(itemReq.getProductId());
            int alreadyReceived = alreadyReceivedMap.getOrDefault(itemReq.getProductId(), 0);
            int remaining = orderedQty - alreadyReceived;

            if (itemReq.getReceivedQuantity() <= 0) {
                throw new IllegalArgumentException("Received quantity must be greater than zero for product ID: " + itemReq.getProductId());
            }
            if (itemReq.getReceivedQuantity() > remaining) {
                throw new IllegalArgumentException("Over-receiving not allowed for product ID " + itemReq.getProductId()
                        + ". Ordered: " + orderedQty + ", already received: " + alreadyReceived
                        + ", remaining: " + remaining + ", attempted: " + itemReq.getReceivedQuantity());
            }
        }

        // F. Generate unique Goods Receipt number
        String grNumber = generateUniqueGrNumber();

        // G. Save GoodsReceipt header
        GoodsReceipt goodsReceipt = new GoodsReceipt();
        goodsReceipt.setGrNumber(grNumber);
        goodsReceipt.setPurchaseOrderId(po.getId());
        goodsReceipt.setReceivedDate(request.getReceivedDate());
        goodsReceipt.setReceivedBy(receivedByUserId);
        goodsReceipt.setRemarks(request.getRemarks());
        goodsReceipt.setCreatedAt(LocalDateTime.now());

        GoodsReceipt savedGoodsReceipt = goodsReceiptRepository.save(goodsReceipt);

        // H. Save GoodsReceiptItem rows
        for (CreateGoodsReceiptRequest.ReceiptItemRequest itemReq : request.getItems()) {
            GoodsReceiptItem grItem = new GoodsReceiptItem();
            grItem.setGoodsReceiptId(savedGoodsReceipt.getId());
            grItem.setProductId(itemReq.getProductId());
            grItem.setReceivedQuantity(itemReq.getReceivedQuantity());
            grItem.setCreatedAt(LocalDateTime.now());
            goodsReceiptItemRepository.save(grItem);
        }

        // I. Update inventory and product stock atomically
        for (CreateGoodsReceiptRequest.ReceiptItemRequest itemReq : request.getItems()) {
            Integer prodId = itemReq.getProductId();
            Integer receivedQty = itemReq.getReceivedQuantity();

            // Inventory
            Inventory inventory = inventoryRepository.findByProductId(prodId).orElse(null);
            if (inventory != null) {
                int currentStock = inventory.getQuantityInStock() != null ? inventory.getQuantityInStock() : 0;
                inventory.setQuantityInStock(currentStock + receivedQty);
                inventory.setLastUpdated(LocalDateTime.now());
            } else {
                inventory = new Inventory();
                inventory.setProductId(prodId);
                inventory.setQuantityInStock(receivedQty);
                inventory.setReorderLevel(DEFAULT_REORDER_LEVEL);
                inventory.setLastUpdated(LocalDateTime.now());
            }
            inventoryRepository.save(inventory);

            // Product
            Product product = productRepository.findById(prodId)
                    .orElseThrow(() -> new IllegalArgumentException("Product with ID " + prodId + " does not exist"));
            int currentProductStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(currentProductStock + receivedQty);
            productRepository.save(product);
        }

        // J. Determine PO completion
        Map<Integer, Integer> currentReceiptMap = new HashMap<>();
        for (CreateGoodsReceiptRequest.ReceiptItemRequest itemReq : request.getItems()) {
            currentReceiptMap.put(itemReq.getProductId(), itemReq.getReceivedQuantity());
        }

        boolean allLinesCompleted = true;
        for (PurchaseOrderItem poItem : poItems) {
            int prodId = poItem.getId().getProductId();
            int orderedQty = poItem.getQuantity();
            int previouslyReceivedQty = alreadyReceivedMap.getOrDefault(prodId, 0);
            int thisReceiptQty = currentReceiptMap.getOrDefault(prodId, 0);
            int totalReceived = previouslyReceivedQty + thisReceiptQty;

            if (totalReceived > orderedQty) {
                throw new IllegalArgumentException("Total received quantity exceeds ordered quantity for product ID: " + prodId);
            }
            if (totalReceived < orderedQty) {
                allLinesCompleted = false;
            }
        }

        if (allLinesCompleted) {
            po.setStatus("Completed");
            purchaseOrderRepository.save(po);
        }

        // M. Return persisted GoodsReceipt
        return savedGoodsReceipt;
    }

    private String generateUniqueGrNumber() {
        String grNumber;
        int attempts = 0;
        do {
            grNumber = "GR-" + System.currentTimeMillis();
            attempts++;
            if (goodsReceiptRepository.existsByGrNumber(grNumber)) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (goodsReceiptRepository.existsByGrNumber(grNumber) && attempts < 10);

        if (goodsReceiptRepository.existsByGrNumber(grNumber)) {
            grNumber = "GR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }
        return grNumber;
    }
}