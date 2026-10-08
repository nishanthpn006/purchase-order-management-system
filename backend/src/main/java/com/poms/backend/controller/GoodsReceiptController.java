package com.poms.backend.controller;

import com.poms.backend.config.OpenApiConfig;
import com.poms.backend.dto.CreateGoodsReceiptRequest;
import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.entity.User;
import com.poms.backend.security.JwtUtil;
import com.poms.backend.service.GoodsReceiptService;
import com.poms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/goods-receipts")
@Tag(name = "Goods Receipts", description = "Delivery verification, received items tracking, and goods receipt notes")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public GoodsReceiptController(GoodsReceiptService goodsReceiptService,
                                  UserService userService,
                                  JwtUtil jwtUtil) {
        this.goodsReceiptService = goodsReceiptService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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

    /**
     * POST /api/goods-receipts
     * Creates a new goods receipt against an approved purchase order.
     * Allowed roles: ADMIN, MANAGER, EMPLOYEE.
     */
    @PostMapping
    @Operation(
            summary = "Create a new goods receipt",
            description = "Protected endpoint. Records goods receipt acknowledging delivered merchandise against an Approved purchase order, increments inventory and product stock, and marks purchase order Completed when fully fulfilled. Allowed roles: ADMIN, MANAGER, EMPLOYEE."
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Goods receipt created successfully",
                    content = @Content(schema = @Schema(implementation = GoodsReceipt.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation error, invalid purchase order status, over-receiving, or unknown product"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<?> createGoodsReceipt(
            @Valid @RequestBody CreateGoodsReceiptRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }

        // Extract the current user from the JWT Bearer token or security context
        String email = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(token);
            } catch (Exception ignored) {
            }
        }
        if (email == null) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                email = auth.getName();
            }
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated user not found"));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated user not found"));
        }

        try {
            GoodsReceipt savedReceipt = goodsReceiptService.createGoodsReceipt(request, userOpt.get().getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReceipt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
    }
}

