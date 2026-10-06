# Changelog

All notable changes to the Purchase Order Management System (POMS) project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - Review-II (In Progress)

### Added
- **Backend Architecture Consolidation**: Consolidated the Spring Boot 3 REST API into `backend/`, standardizing on Java 21, Spring Data JPA, Spring Security, and PostgreSQL while removing the legacy Node.js/Express prototype.
- **Service Layer Test Suite**: Added comprehensive JUnit 5 + Mockito unit tests covering 100% of service methods.
- **Role-Based Access Control (RBAC)**: Role-level authorization (`Admin`, `Manager`, `Employee`) enforced via Spring Security JWT filter.
- **Purchase Order Itemized Details**: Added `GET /api/purchase-orders/{id}` endpoint returning full PO metadata and joined item list.
- **Transactional PO Creation**: Added `POST /api/purchase-orders` endpoint with item total calculations and status workflow.
- **Approval & Status Workflow**: Added `PATCH /api/purchase-orders/{id}/status` endpoint restricted to `Admin` and `Manager` roles.

### Planned
- **Goods Receipt Processing**: Record and validate incoming deliveries against open purchase orders.
- **Inventory Auto-Update**: Automatically update inventory stock levels upon goods receipt confirmation.
- **Reports & Analytics**: Summary reports for procurement spend, vendor performance, and inventory turnover.

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
