# Functional Requirements

## 1. Introduction

The Purchase Order Management System (POMS) is an enterprise application designed to streamline procurement operations, vendor management, product tracking, purchase ordering, inventory control, and delivery receipts.

This document clearly distinguishes between **Currently Implemented (Review-I MVP)** functionality and **Future Scope / Planned Enhancements**.

---

## 2. Currently Implemented Requirements (Review-I MVP)

### FR-01: User Authentication & Session Control

- **FR-01.1**: The system shall authenticate users against the `users` table in MySQL using email and password.
- **FR-01.2**: The system shall verify passwords using `bcrypt` salted hash comparison (`bcrypt.compare`).
- **FR-01.3**: The system shall issue a signed JSON Web Token (JWT) containing user ID, email, and role upon successful login.
- **FR-01.4**: The system shall return a generic 401 error message ("Invalid email or password.") upon authentication failure to prevent account enumeration.
- **FR-01.5**: The system shall enforce client-side route protection, automatically redirecting unauthenticated visitors to the login interface.
- **FR-01.6**: The system shall persist active JWT tokens and decoded user sessions in browser `localStorage`.
- **FR-01.7**: The system shall support user logout, clearing local credentials and redirecting to the login page.

### FR-02: Operations Dashboard

- **FR-02.1**: The system shall compute real-time KPI metrics from MySQL: Total Vendors, Total Products, Total Purchase Orders, and Total Inventory Items.
- **FR-02.2**: The system shall calculate pending order counters and low-stock alert counters.
- **FR-02.3**: The system shall render a Recent Purchase Orders overview table displaying PO Number, Vendor Name, Total Amount, and Status Badge.
- **FR-02.4**: The system shall render an Inventory Summary table displaying Product Name, Quantity in Stock, and Stock Status.

### FR-03: Vendor Management

- **FR-03.1**: The system shall retrieve and display all vendor records from MySQL (`GET /api/vendors`).
- **FR-03.2**: The system shall display vendor details including Vendor Name, Contact Person, Email, Phone, GST Number, and Active/Inactive status.
- **FR-03.3**: The system shall support client-side filtering and real-time text search across vendor fields.

### FR-04: Products Catalog

- **FR-04.1**: The system shall retrieve and display catalog items joined with vendor details (`GET /api/products`).
- **FR-04.2**: The system shall display Product Name, Category, Vendor Name, Unit Price, Unit of Measurement, and Availability Status.
- **FR-04.3**: The system shall support client-side filtering and search across product attributes.

### FR-05: Purchase Orders View

- **FR-05.1**: The system shall retrieve purchase order headers joined with vendor and creator names (`GET /api/purchase-orders`).
- **FR-05.2**: The system shall display PO Number, Vendor Name, Order Date, Expected Delivery Date, Total Amount, and Status Badges (`Pending`, `Approved`, `Completed`, `Rejected`).

### FR-06: Inventory Monitoring

- **FR-06.1**: The system shall retrieve inventory stock records joined with product and vendor details (`GET /api/inventory`).
- **FR-06.2**: The system shall compute stock status dynamically:
  - `In Stock` (Quantity > Reorder Level)
  - `Low Stock` (0 < Quantity ≤ Reorder Level)
  - `Reorder Required` (Quantity = 0)

### FR-07: Goods Receipts Tracking

- **FR-07.1**: The system shall retrieve delivery receipts joined with purchase orders and receiver names (`GET /api/goods-receipts`).
- **FR-07.2**: The system shall display PO Reference, Vendor Name, Received Date, Receiver Name, and Delivery Remarks.

---

## 3. Future Scope & Planned Enhancements (Review-II / Production)

- **FR-08: Full Vendor & Product CRUD**: Interactive creation, updating, and deactivation of vendors and products.
- **FR-09: Interactive PO Creator**: Multi-line item purchase order creation form with automatic total price calculation.
- **FR-10: Goods Receipt Logging**: Interactive receipt entry form linked to pending purchase orders that automatically increments stock levels.
- **FR-11: Role-Based Authorization (RBAC)**: Fine-grained permission checks restricting write actions based on user role (`Admin`, `Manager`, `Employee`).
- **FR-12: PDF Purchase Order Export**: Server-side or client-side PDF document generation for purchase orders.
- **FR-13: Audit Trail**: Dedicated activity log table tracking user actions and timestamps.