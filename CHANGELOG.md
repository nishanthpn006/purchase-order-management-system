# Changelog

All notable changes to the Purchase Order Management System (POMS) project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - Review-II (In Progress)

### Planned
- **Purchase Order Creation**: Full PO creation workflow with vendor and product selection, quantity input, and status tracking.
- **Approval Workflow**: Role-based PO approval and rejection flow for managers.
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
