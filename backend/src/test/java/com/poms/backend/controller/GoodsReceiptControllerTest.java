package com.poms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poms.backend.dto.CreateGoodsReceiptRequest;
import com.poms.backend.entity.GoodsReceipt;
import com.poms.backend.entity.User;
import com.poms.backend.service.GoodsReceiptService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GoodsReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private GoodsReceiptService goodsReceiptService;

    @MockitoBean
    private UserService userService;

    private User adminUser;
    private User managerUser;
    private User employeeUser;
    private GoodsReceipt sampleReceipt;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setEmail("admin@poms.com");
        adminUser.setFullName("Admin User");
        adminUser.setRole("Admin");

        managerUser = new User();
        managerUser.setId(2);
        managerUser.setEmail("manager@poms.com");
        managerUser.setFullName("Manager User");
        managerUser.setRole("Manager");

        employeeUser = new User();
        employeeUser.setId(3);
        employeeUser.setEmail("employee@poms.com");
        employeeUser.setFullName("Employee User");
        employeeUser.setRole("Employee");

        sampleReceipt = new GoodsReceipt();
        sampleReceipt.setId(1);
        sampleReceipt.setGrNumber("GR-2026-001");
        sampleReceipt.setPurchaseOrderId(10);
        sampleReceipt.setReceivedDate(LocalDate.of(2026, 4, 1));
        sampleReceipt.setReceivedBy(1);
        sampleReceipt.setRemarks("Received in good order");
        sampleReceipt.setCreatedAt(LocalDateTime.now());
    }

    private CreateGoodsReceiptRequest createValidRequest() {
        CreateGoodsReceiptRequest request = new CreateGoodsReceiptRequest();
        request.setPurchaseOrderId(10);
        request.setReceivedDate(LocalDate.of(2026, 4, 1));
        request.setRemarks("Received in good order");

        CreateGoodsReceiptRequest.ReceiptItemRequest item = new CreateGoodsReceiptRequest.ReceiptItemRequest();
        item.setProductId(101);
        item.setReceivedQuantity(5);
        request.setItems(List.of(item));

        return request;
    }

    @Test
    @DisplayName("1. POST /api/goods-receipts - valid authenticated Admin returns 201 Created and passes user ID")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void createGoodsReceipt_WhenAdmin_Returns201Created() throws Exception {
        when(userService.findByEmail("admin@poms.com")).thenReturn(Optional.of(adminUser));
        when(goodsReceiptService.createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(1)))
                .thenReturn(sampleReceipt);

        mockMvc.perform(post("/api/goods-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.grNumber", is("GR-2026-001")))
                .andExpect(jsonPath("$.purchaseOrderId", is(10)))
                .andExpect(jsonPath("$.receivedBy", is(1)));

        verify(goodsReceiptService).createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(1));
    }

    @Test
    @DisplayName("2. POST /api/goods-receipts - valid authenticated Manager returns 201 Created and passes user ID")
    @WithMockUser(username = "manager@poms.com", roles = "MANAGER")
    void createGoodsReceipt_WhenManager_Returns201Created() throws Exception {
        sampleReceipt.setReceivedBy(2);
        when(userService.findByEmail("manager@poms.com")).thenReturn(Optional.of(managerUser));
        when(goodsReceiptService.createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(2)))
                .thenReturn(sampleReceipt);

        mockMvc.perform(post("/api/goods-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.receivedBy", is(2)));

        verify(goodsReceiptService).createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(2));
    }

    @Test
    @DisplayName("3. POST /api/goods-receipts - valid authenticated Employee returns 201 Created and passes user ID")
    @WithMockUser(username = "employee@poms.com", roles = "EMPLOYEE")
    void createGoodsReceipt_WhenEmployee_Returns201Created() throws Exception {
        sampleReceipt.setReceivedBy(3);
        when(userService.findByEmail("employee@poms.com")).thenReturn(Optional.of(employeeUser));
        when(goodsReceiptService.createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(3)))
                .thenReturn(sampleReceipt);

        mockMvc.perform(post("/api/goods-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.receivedBy", is(3)));

        verify(goodsReceiptService).createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(3));
    }

    @Test
    @DisplayName("4. POST /api/goods-receipts - unauthenticated request returns 401 Unauthorized")
    void createGoodsReceipt_WhenUnauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(post("/api/goods-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isUnauthorized());

        verify(goodsReceiptService, never()).createGoodsReceipt(any(), any());
    }

    @Test
    @DisplayName("5. POST /api/goods-receipts - invalid payload returns 400 Bad Request")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void createGoodsReceipt_WhenInvalidPayload_Returns400BadRequest() throws Exception {
        when(userService.findByEmail("admin@poms.com")).thenReturn(Optional.of(adminUser));

        // Missing purchaseOrderId and empty items
        CreateGoodsReceiptRequest invalidRequest = new CreateGoodsReceiptRequest();
        invalidRequest.setReceivedDate(LocalDate.now());
        invalidRequest.setItems(Collections.emptyList());

        mockMvc.perform(post("/api/goods-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(goodsReceiptService, never()).createGoodsReceipt(any(), any());
    }

    @Test
    @DisplayName("6. POST /api/goods-receipts - service IllegalArgumentException returns 400 Bad Request")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void createGoodsReceipt_WhenServiceThrowsIllegalArgumentException_Returns400BadRequest() throws Exception {
        when(userService.findByEmail("admin@poms.com")).thenReturn(Optional.of(adminUser));
        when(goodsReceiptService.createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(1)))
                .thenThrow(new IllegalArgumentException("Over-receiving not allowed for product ID 101"));

        mockMvc.perform(post("/api/goods-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Over-receiving not allowed for product ID 101")));

        verify(goodsReceiptService).createGoodsReceipt(any(CreateGoodsReceiptRequest.class), eq(1));
    }

    @Test
    @DisplayName("7. Existing GET /api/goods-receipts returns 200 OK with receipt list")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void getAllGoodsReceipts_WhenAuthenticated_Returns200OK() throws Exception {
        when(goodsReceiptService.getAllGoodsReceipts()).thenReturn(List.of(sampleReceipt));

        mockMvc.perform(get("/api/goods-receipts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].grNumber", is("GR-2026-001")))
                .andExpect(jsonPath("$[0].purchaseOrderId", is(10)));

        verify(goodsReceiptService).getAllGoodsReceipts();
    }

    @Test
    @DisplayName("8. Existing GET /api/goods-receipts/{id} returns 200 OK with receipt")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void getGoodsReceiptById_WhenExists_Returns200OK() throws Exception {
        when(goodsReceiptService.getGoodsReceiptById(1)).thenReturn(Optional.of(sampleReceipt));

        mockMvc.perform(get("/api/goods-receipts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.grNumber", is("GR-2026-001")))
                .andExpect(jsonPath("$.purchaseOrderId", is(10)));

        verify(goodsReceiptService).getGoodsReceiptById(1);
    }

    @Test
    @DisplayName("9. Existing GET /api/goods-receipts/{id} returns 404 Not Found when absent")
    @WithMockUser(username = "admin@poms.com", roles = "ADMIN")
    void getGoodsReceiptById_WhenNotFound_Returns404NotFound() throws Exception {
        when(goodsReceiptService.getGoodsReceiptById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/goods-receipts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Goods receipt not found")));

        verify(goodsReceiptService).getGoodsReceiptById(999);
    }
}
