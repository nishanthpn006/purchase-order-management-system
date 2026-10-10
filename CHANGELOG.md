# Changelog

All notable changes to the Purchase Order Management System (POMS) project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-10-10 (Review-II Production Release)

### Added
- **Cloud Production Deployment**: Deployed production architecture connecting React 19 on Vercel, Spring Boot 3 on Render, and PostgreSQL on Neon.
- **Backend Architecture Consolidation**: Consolidated the Spring Boot 3 REST API into `backend/`, standardizing on Java 21, Spring Data JPA, Spring Security, and PostgreSQL while replacing the legacy Node.js/Express prototype.
- **Service Layer Test Suite**: Comprehensive JUnit 5 + Mockito unit tests covering 100% of service methods, integrated with GitHub Actions CI.
- **Role-Based Access Control (RBAC)**: Role-level authorization (`Admin`, `Manager`, `Employee`) enforced via Spring Security JWT filter and mapped to frontend UI permission guards.
- **Vendor & Product Full CRUD**: Complete create, update, and soft-deactivation endpoints and modal UI workflows.
- **Purchase Order Itemized Details & Creation**: Multi-item PO builder (`POST /api/purchase-orders`), item detail retrieval (`GET /api/purchase-orders/{id}`), and receiving balance tracking (`GET /api/purchase-orders/{id}/receiving-details`).
- **Approval & Status Workflow**: Restricted status transitions (`PATCH /api/purchase-orders/{id}/status`) and order cancellation (`PATCH /api/purchase-orders/{id}/cancel`).
- **Goods Receipt Processing**: Delivery receipt creation (`POST /api/goods-receipts`) linking items to purchase orders with itemized receipt storage (`goods_receipt_items`).
- **Inventory Auto-Update**: Automatic stock level incrementation in `inventory` upon goods receipt processing.
- **Production Security Hardening**: Disabled Swagger UI/OpenAPI in production profile (`application-prod.properties`) and externalized all secrets.

### Planned
- **PDF Purchase Order Export**: Server-side or client-side PDF document generation.
- **Audit Logging Table**: Dedicated activity audit trail tracking critical entity changes and user timestamps.
- **Email Notifications**: Automated alerts on purchase order status changes and low inventory thresholds.

---

## [0.1.0] - 2026-08-11 (Review-I MVP)

### Added

- **JWT Authentication & Security**: End-to-end user authentication with JWT signing and 8-hour session expiration (`POST /api/login`, `GET /api/me`).
- **Protected Routes**: Client-side navigation guarding with React Router (`ProtectedRoute.jsx`) and token persistence via `localStorage`.
- **Operations Dashboard**: Real-time KPI summary (Total Vendors, Products, Purchase Orders, Inventory Stock) and dashboard tables.
- **Vendor Management**: Interactive vendor list view display (`GET /api/vendors`).
- **Product Catalog**: Comprehensive catalog view displaying products and mapped vendor details (`GET /api/products`).
- **Purchase Orders View**: PO status overview with status badges (`Pending`, `Approved`, `Completed`, `Rejected`) (`GET /api/purchase-orders`).
- **Inventory Monitoring**: Live stock monitoring view with computed stock status indicators (`In Stock`, `Low Stock`, `Reorder Required`) (`GET /api/inventory`).
- **Goods Receipts Tracking**: Recorded delivery listings linked to purchase orders (`GET /api/goods-receipts`).
- **MySQL Integration**: Async connection pool setup connecting Node.js Express controllers directly to `purchase_order_db`.
- **Custom CSS Design System**: Responsive enterprise UI shell (`Layout.jsx`, `Sidebar.jsx`, `Navbar.jsx`, `poms.css`).
- **Database Scripts**: Schema definition (`database/schema.sql`) and sample seed script (`database/seed.sql`).

### Security

- **Bcrypt Hashing**: Mandatory `bcrypt.compare` password verification for all user authentications with plaintext fallbacks completely removed.
- **CORS Hardening**: Strict origin-restricted CORS middleware using `FRONTEND_URL` environment configuration.
- **Secret Isolation**: Production credential protection using `.env` and `.env.example` templates.
