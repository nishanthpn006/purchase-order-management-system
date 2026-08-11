# System Architecture

## 1. Introduction

The Purchase Order Management System (POMS) uses a decoupled client-server architecture following a Three-Tier pattern (Presentation, Business Logic, and Data Layer). Each layer has defined responsibilities and communicates over established REST protocols.

---

## 2. System Architecture Overview

```text
+-----------------------------------------------------------------------+
|                         PRESENTATION LAYER                            |
|  Browser  <--->  React 19 + Vite Frontend  <--->  Axios API Client    |
+-----------------------------------------------------------------------+
                                   |
                         HTTP / REST API (JSON)
                                   |
+-----------------------------------------------------------------------+
|                         BUSINESS LOGIC LAYER                          |
|  Node.js + Express  <--->  CORS & Auth Middleware  <---> Controllers  |
+-----------------------------------------------------------------------+
                                   |
                       mysql2/promise Pool Queries
                                   |
+-----------------------------------------------------------------------+
|                             DATA LAYER                                |
|                 MySQL 8 Database (`purchase_order_db`)                |
+-----------------------------------------------------------------------+
```

---

## 3. Layer Specifications

### 1. Presentation Layer (Frontend)

- **Framework**: React 19 built with Vite
- **Routing**: React Router 7 (`BrowserRouter`, `Routes`, `Route`, `Navigate`)
- **State & Context**: `AuthContext` with custom `useAuth` hook and `localStorage` persistence
- **HTTP Client**: Axios with request/response interceptors (`services/api.js`)
- **Styling**: Custom CSS Enterprise Design System (`index.css`, `poms.css`)
- **Icons**: Lucide React

### 2. Business Logic Layer (Backend)

- **Runtime**: Node.js
- **Framework**: Express.js
- **Middleware**:
  - `cors`: Restricted to `FRONTEND_URL` origin
  - `express.json()`: Body parser for incoming JSON payloads
  - `authMiddleware.js`: JWT token extractor and validator
- **Routing & Controllers**:
  - `authRoutes.js` / `authController.js`
  - `dashboardRoutes.js` / `dashboardController.js`
  - `vendorRoutes.js` / `vendorController.js`
  - `productRoutes.js` / `productController.js`
  - `purchaseOrderRoutes.js` / `purchaseOrderController.js`
  - `inventoryRoutes.js` / `inventoryController.js`
  - `goodsReceiptRoutes.js` / `goodsReceiptController.js`

### 3. Data Layer (Database)

- **Engine**: MySQL 8
- **Driver**: `mysql2/promise` (Connection Pooling via `config/db.js`)
- **Database**: `purchase_order_db`
- **Tables**: `users`, `vendors`, `products`, `purchase_orders`, `purchase_order_items`, `inventory`, `goods_receipts`

---

## 4. End-to-End Authentication Architecture

```text
User enters email & password on Login page
                    │
                    ▼
          POST /api/login (Axios)
                    │
                    ▼
     Express authController.login()
                    │
                    ├── 1. Query users table by email
                    ├── 2. Verify password via bcrypt.compare()
                    ├── 3. If invalid -> Return HTTP 401 ("Invalid email or password.")
                    └── 4. If valid -> Sign JWT token with user id, email, role (8h)
                    │
                    ▼
        Return 200 OK + JWT + User Object
                    │
                    ▼
  Frontend AuthContext stores token & user in localStorage
                    │
                    ▼
  Subsequent Protected Requests include Authorization header:
          "Authorization: Bearer <JWT_TOKEN>"
                    │
                    ▼
  Backend authMiddleware verifies token & attaches req.user
```

---

## 5. Technology Stack Summary

| Layer | Technology | Version / Tool |
| --- | --- | --- |
| Client | React / Vite / React Router | React 19, Vite 8, React Router 7 |
| Styling & UI | Custom CSS / Lucide React | `poms.css` design system |
| HTTP Client | Axios | Custom interceptors |
| Server | Node.js / Express.js | Express 4 |
| Database Engine | MySQL | MySQL 8 |
| Database Driver | `mysql2/promise` | Connection pool |
| Security | `bcryptjs` / `jsonwebtoken` | Salted bcrypt compare, signed JWTs |