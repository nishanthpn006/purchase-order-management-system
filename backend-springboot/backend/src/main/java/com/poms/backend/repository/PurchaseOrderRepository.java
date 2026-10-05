package com.poms.backend.repository;

import com.poms.backend.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    long countByStatus(String status);
    List<PurchaseOrder> findByVendorId(Integer vendorId);
}