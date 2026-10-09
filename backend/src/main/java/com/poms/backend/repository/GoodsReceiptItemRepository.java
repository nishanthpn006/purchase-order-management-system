package com.poms.backend.repository;

import com.poms.backend.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Integer> {
    List<GoodsReceiptItem> findByGoodsReceiptId(Integer goodsReceiptId);
    List<GoodsReceiptItem> findByGoodsReceiptIdIn(List<Integer> goodsReceiptIds);

    @Query("""
        SELECT gri.productId, SUM(gri.receivedQuantity)
        FROM GoodsReceiptItem gri
        JOIN GoodsReceipt gr ON gri.goodsReceiptId = gr.id
        WHERE gr.purchaseOrderId = :purchaseOrderId
        GROUP BY gri.productId
    """)
    List<Object[]> sumReceivedQuantitiesByPurchaseOrderId(
        @Param("purchaseOrderId") Integer purchaseOrderId
    );
}
