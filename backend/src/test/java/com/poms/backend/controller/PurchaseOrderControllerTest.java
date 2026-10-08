package com.poms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poms.backend.dto.CreatePurchaseOrderRequest;
import com.poms.backend.dto.UpdateStatusRequest;
import com.poms.backend.entity.PurchaseOrder;
import com.poms.backend.entity.User;
import com.poms.backend.service.PurchaseOrderItemService;
import com.poms.backend.service.PurchaseOrderService;
import com.poms.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .findAndRegisterModules();

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @MockitoBean
    private PurchaseOrderItemService purchaseOrderItemService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private PurchaseOrder samplePo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("admin@poms.com");
        testUser.setFullName("Admin User");
        testUser.setRole("Admin");

        samplePo = new PurchaseOrder();
        samplePo.setId(10);
        samplePo.setPoNumber("PO-2026-999");
        samplePo.setVendorId(2);
        samplePo.setOrderDate(LocalDate.of(2026, 4, 1));
        samplePo.setTotalAmount(new BigDecimal("3000.00"));
        samplePo.setStatus("Pending");
        samplePo.setCreatedBy(1);
        samplePo.setCreatedAt(LocalDateTime.now());
    }

    // ── Milestone Tests with MockMvc ──

    @Test
    @DisplayName("POST /api/purchase-orders succeeds with HTTP 201 Created for authenticated user")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void createPurchaseOrder_Success_Returns201Created() throws Exception {
        when(userService.findByEmail("admin@poms.com")).thenReturn(Optional.of(testUser));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(2);
        request.setOrderDate(LocalDate.of(2026, 4, 1));
        request.setTotalAmount(new BigDecimal("3000.00"));

        CreatePurchaseOrderRequest.OrderItemRequest item1 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item1.setProductId(101);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("1500.00"));
        request.setItems(List.of(item1));

        when(purchaseOrderService.createPurchaseOrder(any(CreatePurchaseOrderRequest.class), eq(1)))
                .thenReturn(samplePo);

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.poNumber", is("PO-2026-999")))
                .andExpect(jsonPath("$.totalAmount", is(3000.00)))
                .andExpect(jsonPath("$.status", is("Pending")));
    }

    @Test
    @DisplayName("POST /api/purchase-orders fails with HTTP 400 Bad Request when validation fails (missing vendorId)")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void createPurchaseOrder_ValidationFailure_MissingVendorId_Returns400() throws Exception {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setOrderDate(LocalDate.of(2026, 4, 1));

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("vendorId and orderDate are required")));
    }

    @Test
    @DisplayName("POST /api/purchase-orders fails with HTTP 400 Bad Request when duplicate product IDs are submitted")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void createPurchaseOrder_DuplicateProductFailure_Returns400() throws Exception {
        when(userService.findByEmail("admin@poms.com")).thenReturn(Optional.of(testUser));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setVendorId(2);
        request.setOrderDate(LocalDate.of(2026, 4, 1));

        CreatePurchaseOrderRequest.OrderItemRequest item1 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item1.setProductId(101);
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("500.00"));

        CreatePurchaseOrderRequest.OrderItemRequest item2 = new CreatePurchaseOrderRequest.OrderItemRequest();
        item2.setProductId(101); // Duplicate
        item2.setQuantity(2);
        item2.setUnitPrice(new BigDecimal("500.00"));

        request.setItems(List.of(item1, item2));

        when(purchaseOrderService.createPurchaseOrder(any(CreatePurchaseOrderRequest.class), eq(1)))
                .thenThrow(new IllegalArgumentException("Duplicate product ID found in purchase order items: 101"));

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Duplicate product ID found in purchase order items: 101")));
    }

    @Test
    @DisplayName("PATCH /api/purchase-orders/:id/status fails with HTTP 400 Bad Request on invalid status transition")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_InvalidStatusTransition_Returns400() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("Completed");

        when(purchaseOrderService.updateStatus(10, "Completed"))
                .thenThrow(new IllegalArgumentException("Invalid status transition from 'Pending' to 'Completed'. Allowed transitions from 'Pending' are: Approved, Rejected"));

        mockMvc.perform(patch("/api/purchase-orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid status transition from 'Pending' to 'Completed'")));
    }

    @Test
    @DisplayName("PATCH /api/purchase-orders/:id/status allows Admin role to update status")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_AdminRole_Allowed() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("Approved");
        samplePo.setStatus("Approved");

        when(purchaseOrderService.updateStatus(10, "Approved")).thenReturn(samplePo);

        mockMvc.perform(patch("/api/purchase-orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Approved")));
    }

    @Test
    @DisplayName("PATCH /api/purchase-orders/:id/status allows Manager role to update status")
    @WithMockUser(roles = "MANAGER")
    void updateStatus_ManagerRole_Allowed() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("Approved");
        samplePo.setStatus("Approved");

        when(purchaseOrderService.updateStatus(10, "Approved")).thenReturn(samplePo);

        mockMvc.perform(patch("/api/purchase-orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Approved")));
    }

    @Test
    @DisplayName("PATCH /api/purchase-orders/:id/status returns HTTP 403 Forbidden for Employee role")
    @WithMockUser(roles = "EMPLOYEE")
    void updateStatus_EmployeeRole_Returns403Forbidden() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("Approved");

        mockMvc.perform(patch("/api/purchase-orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
