package com.poms.backend.controller;

import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.service.GoodsReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    public GoodsReceiptController(GoodsReceiptService goodsReceiptService) {
        this.goodsReceiptService = goodsReceiptService;
    }

    /**
     * GET /api/goods-receipts
     * Returns all goods receipts.
     */
    @GetMapping
    public ResponseEntity<List<GoodsReceipt>> getAllGoodsReceipts() {
        return ResponseEntity.ok(goodsReceiptService.getAllGoodsReceipts());
    }

    /**
     * GET /api/goods-receipts/:id
     * Returns a single goods receipt by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGoodsReceiptById(@PathVariable Integer id) {
        return goodsReceiptService.getGoodsReceiptById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Goods receipt not found")));
    }
}
