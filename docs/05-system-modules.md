# System Modules

## 1. Introduction

The Purchase Order Management System (POMS) architecture is organized into modular business components.

This document details the **Currently Implemented Review-I Modules** along with their purpose, functionality, API endpoints, and database tables, followed by **Future Enhancements**.

---

## 2. Implemented Review-I Modules

### 1. Authentication Module

- **Purpose**: Provides secure login, password verification, and JWT session handling.
- **Implemented Functionality**: `POST /api/login` credentials verification with `bcrypt.compare`, `GET /api/me` token validation, client-side route protection, and session logout.
- **API Endpoints**: `POST /api/login`, `GET /api/me`
- **Database Table**: `users`

### 2. Dashboard Module

- **Purpose**: Delivers a central operations overview for procurement metrics.
- **Implemented Functionality**: Aggregated KPI statistical counters, pending order tracker, low stock alert calculation, recent purchase order table, and inventory overview.
- **API Endpoint**: `GET /api/dashboard/stats`
- **Database Tables**: `vendors`, `products`, `purchase_orders`, `inventory`

### 3. Vendor Management Module

- **Purpose**: Maintains registered supplier directories and contact information.
- **Implemented Functionality**: Vendor listing displaying contact person, email, phone, GST number, and active status, with client-side real-time search.
- **API Endpoint**: `GET /api/vendors`
- **Database Table**: `vendors`

### 4. Product Catalog Module

- **Purpose**: Manages product listings and vendor associations.
- **Implemented Functionality**: Product directory displaying category, vendor name, unit price, stock quantity, unit of measurement, and availability status.
- **API Endpoint**: `GET /api/products`
- **Database Tables**: `products`, `vendors`

### 5. Purchase Order Module

- **Purpose**: Tracks purchase order lifecycles and totals.
- **Implemented Functionality**: Purchase order header listing with PO numbers, vendor names, order dates, expected delivery dates, total amounts, and status badges (`Pending`, `Approved`, `Completed`, `Rejected`).
- **API Endpoint**: `GET /api/purchase-orders`
- **Database Tables**: `purchase_orders`, `vendors`, `users`

### 6. Inventory Module

- **Purpose**: Monitors current warehouse stock levels against safety reorder levels.
- **Implemented Functionality**: Inventory stock list displaying quantity in stock, reorder levels, last updated timestamps, and dynamically computed stock statuses (`In Stock`, `Low Stock`, `Reorder Required`).
- **API Endpoint**: `GET /api/inventory`
- **Database Tables**: `inventory`, `products`, `vendors`

### 7. Goods Receipt Module

- **Purpose**: Records incoming delivery verifications from vendors.
- **Implemented Functionality**: Goods receipt listing linking delivery receipts to PO numbers, received dates, receiver personnel, and remarks.
- **API Endpoint**: `GET /api/goods-receipts`
- **Database Tables**: `goods_receipts`, `purchase_orders`, `users`

---

## 3. Future Modules / Enhancements (Review-II Scope)

- **Vendor & Product CRUD Module**: Full create, update, and delete actions for vendor and product management.
- **Purchase Order Creation Module**: Form builder for generating multi-item purchase orders with itemized price calculation (`purchase_order_items`).
- **Delivery Logging Module**: Receiving form to record goods receipts and automatically increment `inventory.quantity_in_stock`.
- **Reports & Analytics Module**: Exportable PDF/Excel procurement summaries, supplier performance metrics, and monthly spending reports.