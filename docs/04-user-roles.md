# User Roles & Access Control

## 1. Introduction

The Purchase Order Management System (POMS) database schema supports role distinction through the `role` column in the `users` table (`VARCHAR(20)` in PostgreSQL), storing values `'Admin'`, `'Manager'`, and `'Employee'`.

All three roles are **fully implemented, tested, and active in production**.

---

## 2. System Role Specifications

### 1. Administrator (`Admin` / `ROLE_ADMIN`) — IMPLEMENTED

- **Database Value**: `'Admin'`
- **Spring Security Authority**: `ROLE_ADMIN`
- **Backend Authorization**: Full application management permissions, subject to the configured endpoint security rules. Permitted to perform all read, create, update, and deactivation operations across Vendors, Products, Purchase Orders, Inventory, and Goods Receipts.
- **Frontend UI Visibility**: All mutation buttons, action controls, status modal dialogs, and cancellation options are visible and active.

### 2. Procurement Manager (`Manager` / `ROLE_MANAGER`) — IMPLEMENTED

- **Database Value**: `'Manager'`
- **Spring Security Authority**: `ROLE_MANAGER`
- **Backend Authorization**: Full operational procurement permissions. Permitted to create, edit, and deactivate Vendors and Products; create, edit, cancel, and update status of Purchase Orders; and record Goods Receipts (`POST /api/goods-receipts`).
- **Frontend UI Visibility**: All management controls across Vendors, Products, Purchase Orders, and Goods Receipts are visible and interactive (`canManageVendors`, `canManageProducts`, `canManagePurchaseOrders`, `canReceiveGoods`).

### 3. Employee (`Employee` / `ROLE_EMPLOYEE`) — IMPLEMENTED

- **Database Value**: `'Employee'`
- **Spring Security Authority**: `ROLE_EMPLOYEE`
- **Backend Authorization**:
  - **Read Access**: Permitted to view records across all modules (`GET` endpoints for Dashboard, Vendors, Products, Purchase Orders, Inventory, and Goods Receipts).
  - **Purchase Order Creation**: Permitted at the backend API layer to create purchase orders (`POST /api/purchase-orders`), as this endpoint requires valid JWT authentication rather than an Admin/Manager role restriction.
  - **Goods Receipt Entry**: Permitted at the backend API layer to record delivery receipts (`POST /api/goods-receipts`), which is explicitly granted to `ADMIN`, `MANAGER`, and `EMPLOYEE`.
  - **Restricted Operations (HTTP 403 Forbidden)**: Strictly prohibited from editing purchase orders (`PUT /api/purchase-orders/{id}`), cancelling purchase orders (`PATCH /api/purchase-orders/{id}/cancel`), changing purchase order status (`PATCH /api/purchase-orders/{id}/status`), or mutating vendors/products.
- **Frontend UI Visibility**:
  - **Goods Receipts**: The "Receive Items" action modal is visible and interactive (`canReceiveGoods = true`).
  - **Purchase Orders**: Displays a read-only list view. The "Create Purchase Order" button is **not rendered** in the current UI because the client guards this control with `canManagePurchaseOrders` (`isAdmin || isManager`). Edit, Cancel, and Status change actions are also hidden.
  - **Vendors & Products**: Displays read-only directories; creation and modification buttons are hidden (`canManageVendors`, `canManageProducts`).

---

## 3. Role Summary & Permissions Matrix

| Feature / Action | Admin | Manager | Employee (Backend API) | Employee (Frontend UI) |
| :--- | :--- | :--- | :--- | :--- |
| View Dashboard & Catalogues | Allowed | Allowed | Allowed | Visible |
| Create / Edit / Deactivate Vendors | Allowed | Allowed | Forbidden (403) | Controls Hidden |
| Create / Edit / Deactivate Products | Allowed | Allowed | Forbidden (403) | Controls Hidden |
| Create Purchase Order (`POST /api/purchase-orders`) | Allowed | Allowed | **Allowed** | **Button Hidden** (`canManagePurchaseOrders`) |
| Edit Purchase Order (`PUT /api/purchase-orders/{id}`) | Allowed | Allowed | Forbidden (403) | Action Hidden |
| Cancel Purchase Order (`PATCH .../cancel`) | Allowed | Allowed | Forbidden (403) | Action Hidden |
| Change PO Status (`PATCH .../status`) | Allowed | Allowed | Forbidden (403) | Action Hidden |
| Record Goods Receipts (`POST /api/goods-receipts`) | Allowed | Allowed | **Allowed** | **Visible & Active** (`canReceiveGoods`) |

---

## 4. Role-Based Access Control (RBAC) Architecture

- **Token Claims**: Upon successful authentication (`POST /api/login`), [`JwtUtil.java`](file:///d:/Coding/Capstone/purchase-order-management-system/backend/src/main/java/com/poms/backend/security/JwtUtil.java) embeds the granted authority (`ROLE_ADMIN`, `ROLE_MANAGER`, or `ROLE_EMPLOYEE`) directly in the signed JWT payload.
- **Filter Chain Verification**: [`JwtFilter.java`](file:///d:/Coding/Capstone/purchase-order-management-system/backend/src/main/java/com/poms/backend/security/JwtFilter.java) validates incoming Bearer tokens on each HTTP request and populates the `SecurityContextHolder`.
- **Endpoint Authorization**: [`SecurityConfig.java`](file:///d:/Coding/Capstone/purchase-order-management-system/backend/src/main/java/com/poms/backend/security/SecurityConfig.java) enforces role matching using `.hasAnyRole("ADMIN", "MANAGER")` and `.hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")`.
- **Client-Side Authorization**: [`AuthContext.jsx`](file:///d:/Coding/Capstone/purchase-order-management-system/frontend/src/context/AuthContext.jsx) synchronizes user profile claims on application load via `GET /api/me` and provides permission boolean flags (`isAdmin`, `isManager`, `isEmployee`, `canManageVendors`, `canManageProducts`, `canManagePurchaseOrders`, `canReceiveGoods`) for conditional UI component rendering.