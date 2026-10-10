# System Architecture

## 1. Introduction

The Purchase Order Management System (POMS) uses a decoupled client-server architecture following a Three-Tier pattern (Presentation, Business Logic, and Data Layer). Each layer has defined responsibilities and communicates over established REST protocols.

---

## 2. System Architecture Overview

```text
+-----------------------------------------------------------------------+
|                         PRESENTATION LAYER                            |
|    Browser  <--->  React 19 + Vite Frontend (Vercel)  <---> Axios     |
+-----------------------------------------------------------------------+
                                   |
                         HTTPS / REST API (JSON)
                                   |
+-----------------------------------------------------------------------+
|                         BUSINESS LOGIC LAYER                          |
|   Spring Boot 3 + Java 21 (Render) <-> Spring Security <-> JPA Services|
+-----------------------------------------------------------------------+
                                   |
                      PostgreSQL JDBC / TLS Pool
                                   |
+-----------------------------------------------------------------------+
|                             DATA LAYER                                |
|             PostgreSQL 15+ Managed Database (Neon Cloud)              |
+-----------------------------------------------------------------------+
```

---

## 3. Layer Specifications

### 1. Presentation Layer (Frontend — Hosted on Vercel)

- **Framework**: React 19 built with Vite
- **Routing**: React Router 7 (`BrowserRouter`, `Routes`, `Route`, `Navigate`)
- **State & Context**: `AuthContext` with custom `useAuth` hook and `localStorage` session persistence
- **HTTP Client**: Axios with global request bearer token injection and 401 response interceptors (`services/api.js`)
- **Styling**: Custom Enterprise Design System (`index.css`, `poms.css`)
- **Icons**: Lucide React

### 2. Business Logic Layer (Backend — Hosted on Render)

- **Runtime & Language**: Java 21 (Eclipse Temurin JDK)
- **Framework**: Spring Boot 3.4+
- **Security & Authorization**:
  - `SecurityConfig`: Stateless session policy, CSRF disabled for REST API, CORS configuration for client origin
  - `JwtFilter`: Request filter validating Bearer tokens on protected endpoints
  - `CustomUserDetailsService`: Maps database user records to Spring Security authorities (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`)
  - `DaoAuthenticationProvider`: Verifies credentials via `BCryptPasswordEncoder`
  - `JwtUtil`: HMAC-SHA256 token generation and validation (JJWT 0.12.6)
- **Controllers & Endpoints**:
  - `AuthController`: User authentication (`/api/login`) and current user profile (`/api/me`)
  - `DashboardController`: KPI metric calculation (`/api/dashboard/stats`)
  - `VendorController`: Vendor directory and CRUD operations (`/api/vendors/**`)
  - `ProductController`: Product catalog and CRUD operations (`/api/products/**`)
  - `PurchaseOrderController`: Order creation, receiving balances, and lifecycle updates (`/api/purchase-orders/**`)
  - `InventoryController`: Warehouse stock monitoring (`/api/inventory/**`)
  - `GoodsReceiptController`: Goods receipt entry and delivery logs (`/api/goods-receipts/**`)
  - `HealthController`: Automated uptime check (`/api/health`)

### 3. Data Layer (Database — Hosted on Neon)

- **Engine**: PostgreSQL 15+
- **Data Access**: Spring Data JPA with Hibernate ORM
- **Driver**: Official PostgreSQL JDBC Driver
- **Connection**: Encrypted TLS connection with connection pooling
- **Schema Management**: Explicit schema definition via `database/schema.sql` (`spring.jpa.hibernate.ddl-auto=none`)
- **Tables (8 Normalized Tables)**: `users`, `vendors`, `products`, `purchase_orders`, `purchase_order_items`, `inventory`, `goods_receipts`, `goods_receipt_items`

---

## 4. End-to-End Authentication Architecture

```text
User enters email & password on Login page
                    │
                    ▼
          POST /api/login (Axios)
                    │
                    ▼
     Spring Boot AuthController.login()
                    │
                    ├── 1. AuthenticationManager delegates to DaoAuthenticationProvider
                    ├── 2. CustomUserDetailsService loads User by email from PostgreSQL
                    ├── 3. BCryptPasswordEncoder.matches(rawPassword, storedHash)
                    ├── 4. If invalid -> Return HTTP 401 ("Invalid email or password")
                    └── 5. If valid -> JwtUtil.generateToken(userDetails) with role claim
                    │
                    ▼
        Return 200 OK + Signed JWT Token
                    │
                    ▼
  Frontend AuthContext stores token & fetches profile via GET /api/me
                    │
                    ▼
  Subsequent Protected Requests include Authorization header:
          "Authorization: Bearer <JWT_TOKEN>"
                    │
                    ▼
  Backend JwtFilter extracts token, validates claims, sets SecurityContextHolder
```

---

## 5. Technology Stack Summary

| Layer | Technology | Implementation Details |
| :--- | :--- | :--- |
| **Client Hosting** | Vercel | Single-Page Application (SPA) with route rewrites |
| **Frontend Framework** | React 19 + Vite | Component architecture with React Router 7 |
| **HTTP Client** | Axios | Custom JWT interceptors & global 401 redirect |
| **Server Hosting** | Render | Docker containerized deployment, binding to dynamic `$PORT` |
| **Backend Framework** | Spring Boot 3 + Java 21 | Modular REST API with Spring Data JPA |
| **Security** | Spring Security + JJWT | Stateless JWT auth, BCrypt password hashing, RBAC |
| **Database** | PostgreSQL 15+ on Neon | Fully managed cloud relational database |
| **API Documentation** | SpringDoc OpenAPI 3 / Swagger | Local development only (`http://localhost:5000/swagger-ui.html`) |
| **CI / Automation** | GitHub Actions | Automated build, test, and container packaging on push/PR |