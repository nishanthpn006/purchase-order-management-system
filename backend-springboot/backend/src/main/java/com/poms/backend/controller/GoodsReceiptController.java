package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.service.GoodsReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods-receipts")
@Tag(name = "Goods Receipts", description = "Delivery verification, received items tracking, and goods receipt notes")
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
    @Operation(
            summary = "List all goods receipts",
            description = "Protected endpoint. Retrieves all recorded goods receipts acknowledging delivered merchandise against purchase orders. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of goods receipts retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GoodsReceipt.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<List<GoodsReceipt>> getAllGoodsReceipts() {
        return ResponseEntity.ok(goodsReceiptService.getAllGoodsReceipts());
    }

    /**
     * GET /api/goods-receipts/:id
     * Returns a single goods receipt by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get goods receipt by ID",
            description = "Protected endpoint. Retrieves details of a specific goods receipt note by its primary key ID. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goods receipt found and returned",
                    content = @Content(schema = @Schema(implementation = GoodsReceipt.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "404", description = "Goods receipt not found with specified ID")
    })
    public ResponseEntity<?> getGoodsReceiptById(
            @Parameter(description = "Primary key ID of the goods receipt", required = true, example = "1")
            @PathVariable Integer id) {
        return goodsReceiptService.getGoodsReceiptById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Goods receipt not found")));
    }
}
