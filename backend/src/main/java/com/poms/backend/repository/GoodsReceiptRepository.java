package com.poms.backend.repository;

import com.poms.backend.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Integer> {
    Optional<GoodsReceipt> findByGrNumber(String grNumber);
    List<GoodsReceipt> findByPurchaseOrderId(Integer purchaseOrderId);
    boolean existsByGrNumber(String grNumber);
}