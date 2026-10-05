package com.poms.backend.service;

import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.repository.GoodsReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public GoodsReceiptService(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
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
}