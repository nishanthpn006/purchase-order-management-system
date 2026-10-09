package com.poms.backend.service;

import com.poms.backend.dto.CreateGoodsReceiptRequest;
import com.poms.backend.entity.*;
import com.poms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private GoodsReceiptItemRepository goodsReceiptItemRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GoodsReceiptService goodsReceiptService;

    private GoodsReceipt sampleReceipt;
    private PurchaseOrder approvedPo;
    private PurchaseOrderItem poItem1;
    private PurchaseOrderItem poItem2;
    private Product product1;
    private Product product2;
    private Inventory inventory1;

    @BeforeEach
    void setUp() {
        sampleReceipt = new GoodsReceipt();
        sampleReceipt.setId(1);
        sampleReceipt.setGrNumber("GR-2026-001");
        sampleReceipt.setPurchaseOrderId(5);
        sampleReceipt.setReceivedDate(LocalDate.of(2026, 3, 10));
        sampleReceipt.setReceivedBy(2);
        sampleReceipt.setRemarks("Received all items in good condition");
        sampleReceipt.setCreatedAt(LocalDateTime.now());

        approvedPo = new PurchaseOrder();
        approvedPo.setId(10);
        approvedPo.setPoNumber("PO-2026-001");
        approvedPo.setStatus("Approved");

        poItem1 = new PurchaseOrderItem();
        poItem1.setId(new PurchaseOrderItemId(10, 101));
        poItem1.setQuantity(10);

        poItem2 = new PurchaseOrderItem();
        poItem2.setId(new PurchaseOrderItemId(10, 102));
        poItem2.setQuantity(5);

        product1 = new Product();
        product1.setId(101);
        product1.setStockQuantity(20);

        product2 = new Product();
        product2.setId(102);
        product2.setStockQuantity(50);

        inventory1 = new Inventory();
        inventory1.setId(1);
        inventory1.setProductId(101);
        inventory1.setQuantityInStock(15);
        inventory1.setReorderLevel(10);
    }

    // ── Existing Method Tests ──

    @Test
    @DisplayName("getAllGoodsReceipts returns receipt list when receipts exist")
    void getAllGoodsReceipts_WhenReceiptsExist_ReturnsList() {
        when(goodsReceiptRepository.findAll()).thenReturn(List.of(sampleReceipt));

        List<GoodsReceipt> result = goodsReceiptService.getAllGoodsReceipts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getPurchaseOrderId());
        assertEquals("Received all items in good condition", result.get(0).getRemarks());
        verify(goodsReceiptRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllGoodsReceipts returns empty list when no receipts exist")
    void getAllGoodsReceipts_WhenNoReceiptsExist_ReturnsEmptyList() {
        when(goodsReceiptRepository.findAll()).thenReturn(Collections.emptyList());

        List<GoodsReceipt> result = goodsReceiptService.getAllGoodsReceipts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(goodsReceiptRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getGoodsReceiptById returns receipt when receipt exists")
    void getGoodsReceiptById_WhenReceiptExists_ReturnsReceipt() {
        when(goodsReceiptRepository.findById(1)).thenReturn(Optional.of(sampleReceipt));

        Optional<GoodsReceipt> result = goodsReceiptService.getGoodsReceiptById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(5, result.get().getPurchaseOrderId());
        assertEquals(LocalDate.of(2026, 3, 10), result.get().getReceivedDate());
        verify(goodsReceiptRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getGoodsReceiptById returns empty Optional when receipt does not exist")
    void getGoodsReceiptById_WhenReceiptDoesNotExist_ReturnsEmpty() {
        when(goodsReceiptRepository.findById(999)).thenReturn(Optional.empty());

        Optional<GoodsReceipt> result = goodsReceiptService.getGoodsReceiptById(999);

        assertFalse(result.isPresent());
        verify(goodsReceiptRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("saveGoodsReceipt persists and returns saved goods receipt")
    void saveGoodsReceipt_Success_ReturnsSavedReceipt() {
        when(goodsReceiptRepository.save(sampleReceipt)).thenReturn(sampleReceipt);

        GoodsReceipt result = goodsReceiptService.saveGoodsReceipt(sampleReceipt);

        assertNotNull(result);
        assertEquals(5, result.getPurchaseOrderId());
        assertEquals("Received all items in good condition", result.getRemarks());
        verify(goodsReceiptRepository, times(1)).save(sampleReceipt);
    }

    // ── Milestone Tests for createGoodsReceipt ──

    @Test
    @DisplayName("1. createGoodsReceipt when PO is Approved succeeds and updates stock and inventory")
    void createGoodsReceipt_WhenPOIsApproved_Succeeds() {
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> {
            GoodsReceipt gr = invocation.getArgument(0);
            gr.setId(50);
            return gr;
        });
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        GoodsReceipt result = goodsReceiptService.createGoodsReceipt(request, 1);

        assertNotNull(result);
        assertEquals(50, result.getId());
        assertEquals(10, result.getPurchaseOrderId());
        assertEquals(1, result.getReceivedBy());
        verify(goodsReceiptRepository).save(any(GoodsReceipt.class));
        verify(goodsReceiptItemRepository).save(any(GoodsReceiptItem.class));

        // Verify inventory incremented: 15 + 5 = 20
        ArgumentCaptor<Inventory> invCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(invCaptor.capture());
        assertEquals(20, invCaptor.getValue().getQuantityInStock());

        // Verify product stock incremented: 20 + 5 = 25
        ArgumentCaptor<Product> prodCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(prodCaptor.capture());
        assertEquals(25, prodCaptor.getValue().getStockQuantity());
    }

    @Test
    @DisplayName("2. createGoodsReceipt when PO is Pending throws IllegalArgumentException")
    void createGoodsReceipt_WhenPOIsPending_ThrowsIllegalArgumentException() {
        approvedPo.setStatus("Pending");
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));

        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Goods receipt is allowed only for Approved purchase orders"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. createGoodsReceipt when PO is Rejected throws IllegalArgumentException")
    void createGoodsReceipt_WhenPOIsRejected_ThrowsIllegalArgumentException() {
        approvedPo.setStatus("Rejected");
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));

        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Goods receipt is allowed only for Approved purchase orders"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("4. createGoodsReceipt when PO is Completed throws IllegalArgumentException")
    void createGoodsReceipt_WhenPOIsCompleted_ThrowsIllegalArgumentException() {
        approvedPo.setStatus("Completed");
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));

        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Goods receipt is allowed only for Approved purchase orders"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("5. createGoodsReceipt when PO is Cancelled throws IllegalArgumentException")
    void createGoodsReceipt_WhenPOIsCancelled_ThrowsIllegalArgumentException() {
        approvedPo.setStatus("Cancelled");
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));

        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Goods receipt is allowed only for Approved purchase orders"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("6. createGoodsReceipt when purchase order does not exist throws IllegalArgumentException")
    void createGoodsReceipt_WhenPurchaseOrderDoesNotExist_ThrowsIllegalArgumentException() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());

        CreateGoodsReceiptRequest request = buildRequest(999, 101, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Purchase order with ID 999 does not exist"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("7. createGoodsReceipt when items are empty throws IllegalArgumentException")
    void createGoodsReceipt_WhenItemsAreEmpty_ThrowsIllegalArgumentException() {
        CreateGoodsReceiptRequest request = new CreateGoodsReceiptRequest();
        request.setPurchaseOrderId(10);
        request.setReceivedDate(LocalDate.now());
        request.setItems(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Goods receipt must contain at least one line item"));
    }

    @Test
    @DisplayName("8. createGoodsReceipt when duplicate products are submitted throws IllegalArgumentException")
    void createGoodsReceipt_WhenDuplicateProductsAreSubmitted_ThrowsIllegalArgumentException() {
        CreateGoodsReceiptRequest request = new CreateGoodsReceiptRequest();
        request.setPurchaseOrderId(10);
        request.setReceivedDate(LocalDate.now());

        CreateGoodsReceiptRequest.ReceiptItemRequest itemA = new CreateGoodsReceiptRequest.ReceiptItemRequest();
        itemA.setProductId(101);
        itemA.setReceivedQuantity(2);

        CreateGoodsReceiptRequest.ReceiptItemRequest itemB = new CreateGoodsReceiptRequest.ReceiptItemRequest();
        itemB.setProductId(101);
        itemB.setReceivedQuantity(3);

        request.setItems(List.of(itemA, itemB));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Duplicate product ID found in receipt items: 101"));
    }

    @Test
    @DisplayName("9. createGoodsReceipt when product is not part of PO throws IllegalArgumentException")
    void createGoodsReceipt_WhenProductIsNotPartOfPO_ThrowsIllegalArgumentException() {
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));

        CreateGoodsReceiptRequest request = buildRequest(10, 999, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Product ID 999 is not part of purchase order 10"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("10. createGoodsReceipt when received quantity exceeds remaining throws IllegalArgumentException")
    void createGoodsReceipt_WhenReceivedQuantityExceedsRemaining_ThrowsIllegalArgumentException() {
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1)); // qty=10
        // Already received: 6, remaining: 4
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10))
                .thenReturn(List.<Object[]>of(new Object[]{101, 6L}));

        // Request receives 5 -> exceeds remaining 4
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Over-receiving not allowed for product ID 101"));
        verify(goodsReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("11. createGoodsReceipt when partial receipt is valid leaves PO Approved")
    void createGoodsReceipt_WhenPartialReceiptIsValid_LeavesPOApproved() {
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 4); // PO item qty=10, receives 4

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        GoodsReceipt result = goodsReceiptService.createGoodsReceipt(request, 1);

        assertNotNull(result);
        assertEquals("Approved", approvedPo.getStatus());
        verify(purchaseOrderRepository, never()).save(approvedPo);
    }

    @Test
    @DisplayName("12. createGoodsReceipt when final receipt completes all PO items changes PO to Completed")
    void createGoodsReceipt_WhenFinalReceiptCompletesAllPOItems_ChangesPOToCompleted() {
        // Ordered: 10, Already received: 7, Now receiving: 3
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 3);

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10))
                .thenReturn(List.<Object[]>of(new Object[]{101, 7L}));
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        GoodsReceipt result = goodsReceiptService.createGoodsReceipt(request, 1);

        assertNotNull(result);
        assertEquals("Completed", approvedPo.getStatus());
        verify(purchaseOrderRepository).save(approvedPo);
    }

    @Test
    @DisplayName("13. createGoodsReceipt when full receipt is submitted at once changes PO to Completed")
    void createGoodsReceipt_WhenFullReceiptIsSubmittedAtOnce_ChangesPOToCompleted() {
        // Ordered: 10, Already received: 0, Now receiving: 10
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 10);

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        GoodsReceipt result = goodsReceiptService.createGoodsReceipt(request, 1);

        assertNotNull(result);
        assertEquals("Completed", approvedPo.getStatus());
        verify(purchaseOrderRepository).save(approvedPo);
    }

    @Test
    @DisplayName("14. createGoodsReceipt when inventory exists increments existing inventory")
    void createGoodsReceipt_WhenInventoryExists_IncrementsExistingInventory() {
        inventory1.setQuantityInStock(25);
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        goodsReceiptService.createGoodsReceipt(request, 1);

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        assertEquals(30, captor.getValue().getQuantityInStock());
        assertEquals(1, captor.getValue().getId());
    }

    @Test
    @DisplayName("15. createGoodsReceipt when inventory does not exist creates inventory row")
    void createGoodsReceipt_WhenInventoryDoesNotExist_CreatesInventoryRow() {
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 8);

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.empty());
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        goodsReceiptService.createGoodsReceipt(request, 1);

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        assertEquals(101, captor.getValue().getProductId());
        assertEquals(8, captor.getValue().getQuantityInStock());
        assertEquals(10, captor.getValue().getReorderLevel());
        assertNotNull(captor.getValue().getLastUpdated());
    }

    @Test
    @DisplayName("16. createGoodsReceipt when product does not exist throws IllegalArgumentException")
    void createGoodsReceipt_WhenProductDoesNotExist_ThrowsIllegalArgumentException() {
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 5);

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));
        assertTrue(ex.getMessage().contains("Product with ID 101 does not exist"));
    }

    @Test
    @DisplayName("17. createGoodsReceipt when failure occurs does not continue with later mutations")
    void createGoodsReceipt_WhenFailureOccurs_DoesNotContinueWithLaterMutations() {
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 20); // Exceeds PO qty 10

        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> goodsReceiptService.createGoodsReceipt(request, 1));

        verify(goodsReceiptRepository, never()).save(any());
        verify(goodsReceiptItemRepository, never()).save(any());
        verify(inventoryRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("18. createGoodsReceipt when multiple PO items are partially received calculates completion correctly")
    void createGoodsReceipt_WhenMultiplePOItemsArePartiallyReceived_CalculatesCompletionCorrectly() {
        // PO has 2 items: 101 (qty=10) and 102 (qty=5)
        when(purchaseOrderRepository.findById(10)).thenReturn(Optional.of(approvedPo));
        when(purchaseOrderItemRepository.findByIdPurchaseOrderId(10)).thenReturn(List.of(poItem1, poItem2));
        when(goodsReceiptItemRepository.sumReceivedQuantitiesByPurchaseOrderId(10)).thenReturn(Collections.emptyList());
        when(goodsReceiptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.findByProductId(101)).thenReturn(Optional.of(inventory1));
        when(productRepository.findById(101)).thenReturn(Optional.of(product1));

        // Submit only item 101 fully received (10), but item 102 is not received yet
        CreateGoodsReceiptRequest request = buildRequest(10, 101, 10);

        GoodsReceipt result = goodsReceiptService.createGoodsReceipt(request, 1);

        assertNotNull(result);
        // PO must still be Approved because item 102 has 0 / 5 received
        assertEquals("Approved", approvedPo.getStatus());
        verify(purchaseOrderRepository, never()).save(approvedPo);
    }

    private CreateGoodsReceiptRequest buildRequest(int poId, int productId, int qty) {
        CreateGoodsReceiptRequest request = new CreateGoodsReceiptRequest();
        request.setPurchaseOrderId(poId);
        request.setReceivedDate(LocalDate.now());
        request.setRemarks("Standard receipt");

        CreateGoodsReceiptRequest.ReceiptItemRequest item = new CreateGoodsReceiptRequest.ReceiptItemRequest();
        item.setProductId(productId);
        item.setReceivedQuantity(qty);
        request.setItems(List.of(item));

        return request;
    }
}
