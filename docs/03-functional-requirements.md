# Functional Requirements

## 1. Introduction

The Purchase Order Management System (POMS) is an enterprise application designed to streamline procurement operations, vendor management, product tracking, purchase ordering, inventory control, and delivery receipts.

This document clearly distinguishes between **Currently Implemented (Review-I MVP)** functionality and **Future Scope / Planned Enhancements**.

---

## 2. Implemented Functional Requirements (Production Status)

### FR-01: User Authentication & Session Control

- **FR-01.1**: The system shall authenticate users against the `users` table in PostgreSQL using email and password.
- **FR-01.2**: The system shall verify passwords using Spring Security `BCryptPasswordEncoder` salted hash comparison.
- **FR-01.3**: The system shall issue a signed JSON Web Token (JWT) containing user email and role authority claim upon successful login (`POST /api/login`).
- **FR-01.4**: The system shall return a generic 401 error message ("Invalid email or password") upon authentication failure to prevent account enumeration.
- **FR-01.5**: The system shall enforce client-side route protection, automatically redirecting unauthenticated visitors to the login interface.
- **FR-01.6**: The system shall persist active JWT tokens and decoded user sessions in browser `localStorage`.
- **FR-01.7**: The system shall support user logout, clearing local credentials and redirecting to the login page.

### FR-02: Operations Dashboard

- **FR-02.1**: The system shall compute real-time KPI metrics from PostgreSQL: Total Vendors, Total Products, Total Purchase Orders, and Total Inventory Items (`GET /api/dashboard/stats`).
- **FR-02.2**: The system shall calculate pending order counters and low-stock alert counters.
- **FR-02.3**: The system shall render a Recent Purchase Orders overview table displaying PO Number, Vendor Name, Total Amount, and Status Badge.
- **FR-02.4**: The system shall render an Inventory Summary table displaying Product Name, Quantity in Stock, and Stock Status.

### FR-03: Vendor Management & CRUD

- **FR-03.1**: The system shall retrieve and display all vendor records from PostgreSQL (`GET /api/vendors`, `GET /api/vendors/{id}`).
- **FR-03.2**: The system shall display vendor details including Vendor Name, Contact Person, Email, Phone, GST Number, and Active/Inactive status.
- **FR-03.3**: The system shall support client-side filtering and real-time text search across vendor fields.
- **FR-03.4**: The system shall allow authorized roles (Admin, Manager) to create new vendors (`POST /api/vendors`), edit vendor profiles (`PUT /api/vendors/{id}`), and soft-deactivate vendors (`PATCH /api/vendors/{id}/deactivate`).

### FR-04: Products Catalog & CRUD

- **FR-04.1**: The system shall retrieve and display catalog items joined with vendor details (`GET /api/products`, `GET /api/products/{id}`).
- **FR-04.2**: The system shall display Product Name, Category, Vendor Name, Unit Price, Stock Quantity, Unit of Measurement, and Availability Status.
- **FR-04.3**: The system shall support client-side filtering and search across product attributes.
- **FR-04.4**: The system shall allow authorized roles (Admin, Manager) to create products (`POST /api/products`), edit product details (`PUT /api/products/{id}`), and deactivate products (`PATCH /api/products/{id}/deactivate`).

### FR-05: Purchase Order Management & Lifecycle

- **FR-05.1**: The system shall retrieve purchase order headers joined with vendor details (`GET /api/purchase-orders`, `GET /api/purchase-orders/{id}`).
- **FR-05.2**: The system shall display PO Number, Vendor Name, Order Date, Expected Delivery Date, Total Amount, and Status Badges (`Pending`, `Approved`, `Completed`, `Rejected`).
- **FR-05.3**: The system shall provide an itemized balance view for order receiving verification (`GET /api/purchase-orders/{id}/receiving-details`).
- **FR-05.4**: The system shall allow creation of multi-item purchase orders with automatic total price calculation (`POST /api/purchase-orders`). At the backend API layer, any authenticated role (including Employee) is authorized to invoke this endpoint; in the current frontend UI, the creation form is exposed to Admin and Manager via `canManagePurchaseOrders`.
- **FR-05.5**: The system shall restrict purchase order editing (`PUT /api/purchase-orders/{id}`), status approval/rejection (`PATCH /api/purchase-orders/{id}/status`), and order cancellation (`PATCH /api/purchase-orders/{id}/cancel`) exclusively to Admin and Manager roles.

### FR-06: Inventory Monitoring

- **FR-06.1**: The system shall retrieve inventory stock records joined with product and vendor details (`GET /api/inventory`, `GET /api/inventory/{id}`).
- **FR-06.2**: The system shall compute stock status dynamically:
  - `In Stock` (Quantity > Reorder Level)
  - `Low Stock` (0 < Quantity ≤ Reorder Level)
  - `Reorder Required` (Quantity = 0)

### FR-07: Goods Receipt Logging & Inventory Updates

- **FR-07.1**: The system shall retrieve delivery receipts joined with purchase orders and receiver names (`GET /api/goods-receipts`, `GET /api/goods-receipts/{id}`).
- **FR-07.2**: The system shall allow authorized roles (Admin, Manager, Employee) to record received items against open purchase orders (`POST /api/goods-receipts`).
- **FR-07.3**: The system shall persist itemized delivered quantities in the `goods_receipt_items` table.
- **FR-07.4**: The system shall automatically increment warehouse stock levels in `inventory.quantity_in_stock` upon confirmed receipt.

### FR-08: Role-Based Access Control (RBAC)

- **FR-08.1**: The system shall enforce endpoint authorization at the Spring Security filter chain layer using standard authority prefixes (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`).
- **FR-08.2**: The frontend application shell shall conditionally render mutation controls based on the authenticated user's assigned role.

---

## 3. Future Scope & Planned Enhancements

- **FR-09: PDF Purchase Order Export**: Automated generation of downloadable PDF documents for purchase orders.
- **FR-10: Audit Trail Logging Table**: Dedicated activity log table tracking entity changes, user identities, and timestamps.
- **FR-11: Email & Webhook Alerts**: Automated notifications for order approval transitions and low stock warnings.