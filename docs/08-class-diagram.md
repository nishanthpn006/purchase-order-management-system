# Class Diagram & Module Structure

## 1. Introduction

The Class and Module Structure describes the static organization of the Purchase Order Management System (POMS), representing the relationship between controllers, middleware, data services, and database entities.

---

## 2. Diagram Reference

![Class Diagram](../diagrams/class-diagram.png)

---

## 3. Class & Component Specifications

### 1. Controllers & Handlers (`backend/src/controllers/`)

- `authController`: Manages login credential validation, bcrypt hash comparison, and JWT token issuance.
- `dashboardController`: Queries and aggregates statistical KPI metrics from MySQL.
- `vendorController`: Handles retrieval of vendor listings and supplier information.
- `productController`: Manages catalog item queries joined with vendor data.
- `purchaseOrderController`: Handles purchase order header queries and vendor/user details.
- `inventoryController`: Manages inventory stock queries and dynamically computes stock status.
- `goodsReceiptController`: Handles delivery verification receipt queries.

### 2. Middleware (`backend/src/middlewares/`)

- `authMiddleware`: Extracts bearer tokens from authorization headers, validates JWT signatures, and attaches decoded user claims to request objects (`req.user`).

### 3. Database Layer (`backend/src/config/db.js`)

- `db`: Encapsulates `mysql2/promise` connection pool initialized from environment variables (`DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`).

### 4. Client-Side Services & Context (`frontend/src/`)

- `api.js`: Axios instance with global JWT request interceptor and automatic 401 response interceptor.
- `AuthContext.jsx` / `useAuth.js`: React context provider and hook managing persistent user state and login/logout procedures.