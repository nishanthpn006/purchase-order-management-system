# System Modules

## 1. Introduction

The Purchase Order Management System (POMS) architecture is organized into modular business components.

This document details the **Currently Implemented Review-I Modules** along with their purpose, functionality, API endpoints, and database tables, followed by **Future Enhancements**.

---

## 2. Implemented Enterprise Modules

### 1. Authentication Module

- **Purpose**: Provides secure login, password verification, and JWT session handling.
- **Implemented Functionality**: `POST /api/login` credentials verification with `BCryptPasswordEncoder`, `GET /api/me` token validation and profile loading, client-side route protection, and session logout.
- **API Endpoints**: `POST /api/login`, `GET /api/me`
- **Database Table**: `users`

### 2. Dashboard Module

- **Purpose**: Delivers a central operations overview for procurement metrics.
- **Implemented Functionality**: Aggregated KPI statistical counters, pending order tracker, low stock alert calculation, recent purchase order table, and inventory overview.
- **API Endpoint**: `GET /api/dashboard/stats`
- **Database Tables**: `vendors`, `products`, `purchase_orders`, `inventory`

### 3. Vendor Management Module

- **Purpose**: Maintains registered supplier directories and contact information.
- **Implemented Functionality**: Full vendor lifecycle management including directory listing, single vendor details, creation, profile updating, and soft deactivation.
- **API Endpoints**: `GET /api/vendors`, `GET /api/vendors/{id}`, `POST /api/vendors`, `PUT /api/vendors/{id}`, `PATCH /api/vendors/{id}/deactivate`
- **Database Table**: `vendors`

### 4. Product Catalog Module

- **Purpose**: Manages product listings and vendor associations.
- **Implemented Functionality**: Comprehensive product catalog management with unit prices, units of measurement, availability status, vendor mapping, creation, updating, and deactivation.
- **API Endpoints**: `GET /api/products`, `GET /api/products/{id}`, `POST /api/products`, `PUT /api/products/{id}`, `PATCH /api/products/{id}/deactivate`
- **Database Tables**: `products`, `vendors`

### 5. Purchase Order Module

- **Purpose**: Tracks purchase order lifecycles, item lines, and procurement workflows.
- **Implemented Functionality**: Multi-item purchase order creation (`POST`), itemized order details (`GET /api/purchase-orders/{id}`), delivery balance calculations (`GET /api/purchase-orders/{id}/receiving-details`), order editing (`PUT`), order cancellation (`PATCH .../cancel`), and status updates (`PATCH .../status`).
  - *Authorization Note*: Backend API permits PO creation for all authenticated users (including Employee); the current frontend UI renders the creation button for Admin and Manager. Status modification and cancellation are restricted to Admin and Manager.
- **API Endpoints**: `GET /api/purchase-orders`, `GET /api/purchase-orders/{id}`, `GET /api/purchase-orders/{id}/receiving-details`, `POST /api/purchase-orders`, `PUT /api/purchase-orders/{id}`, `PATCH /api/purchase-orders/{id}/cancel`, `PATCH /api/purchase-orders/{id}/status`
- **Database Tables**: `purchase_orders`, `purchase_order_items`, `vendors`, `users`, `products`

### 6. Inventory Module

- **Purpose**: Monitors current warehouse stock levels against safety reorder levels.
- **Implemented Functionality**: Inventory stock list displaying quantity in stock, reorder levels, last updated timestamps, single item queries, and dynamically computed stock statuses (`In Stock`, `Low Stock`, `Reorder Required`). Stock quantities auto-increment upon Goods Receipt confirmation.
- **API Endpoints**: `GET /api/inventory`, `GET /api/inventory/{id}`
- **Database Tables**: `inventory`, `products`, `vendors`

### 7. Goods Receipt Module

- **Purpose**: Records incoming delivery verifications from vendors and updates stock levels.
- **Implemented Functionality**: Delivery receipt creation (`POST /api/goods-receipts`) recording received items, updating delivery balance against open POs, storing itemized lines in `goods_receipt_items`, and automatically incrementing warehouse quantities in `inventory`.
  - *Authorization Note*: Fully accessible to Admin, Manager, and Employee across backend API and frontend UI.
- **API Endpoints**: `GET /api/goods-receipts`, `GET /api/goods-receipts/{id}`, `POST /api/goods-receipts`
- **Database Tables**: `goods_receipts`, `goods_receipt_items`, `purchase_orders`, `products`, `users`, `inventory`

---

## 3. Future Modules & Enhancements

- **PDF Purchase Order Export**: Automated generation of branded, downloadable PDF documents for purchase orders.
- **Audit Logging Table**: Dedicated activity audit trail tracking critical entity changes, user identities, and timestamps.
- **Automated Notifications Module**: Email and webhook alerts for pending order approvals and low stock warnings.