# Purchase Order Management System (POMS)

> An enterprise-grade procurement and purchase order management web application connecting React, Spring Boot, and PostgreSQL.

---

- **Live Frontend**: [https://purchase-order-management-system-psi.vercel.app/](https://purchase-order-management-system-psi.vercel.app/)
- **Backend Health**: [https://poms-backend-z882.onrender.com/api/health](https://poms-backend-z882.onrender.com/api/health)
- **Local Swagger OpenAPI Docs**: [http://localhost:5000/swagger-ui.html](http://localhost:5000/swagger-ui.html) *(enabled for local development; intentionally disabled in production for security hardening)*

---

## Table of Contents

- [Overview](#overview)
- [Architecture Diagram](#architecture-diagram)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Running Tests](#running-tests)
- [Deployment](#deployment)
- [Folder Structure](#folder-structure)
- [Future Enhancements](#future-enhancements)
- [License](#license)
- [Author & Contact](#author--contact)

---

## Overview

Manual procurement workflows reliant on static spreadsheets and paper approvals lead to operational bottlenecks, misplaced orders, and untracked inventory levels. **POMS** addresses these challenges through a centralized, role-aware management system that connects procurement actions directly to live inventory and database records.

---

## Architecture Diagram

![System Architecture](diagrams/system-architecture.png)

---

## Tech Stack

### Frontend

- **Framework**: React 19 + Vite
- **Routing**: React Router 7 (`BrowserRouter`, `Routes`, `Route`, `Navigate`)
- **HTTP Client**: Axios (with custom auth & 401 response interceptors)
- **Icons**: Lucide React
- **Styling**: Custom CSS Enterprise Design System (`index.css`, `poms.css`)

### Backend

- **Runtime & Language**: Java 21
- **Framework**: Spring Boot 3
- **ORM & Data**: Spring Data JPA / Hibernate
- **Security**: Spring Security + JJWT (JSON Web Token signed sessions)
- **API Documentation**: SpringDoc OpenAPI 3 / Swagger UI
- **Build Tool**: Maven with Maven Wrapper (`mvnw` / `mvnw.cmd`)

### Database

- **Engine**: PostgreSQL
- **Database Name**: `purchase_order_db`
- **Default Schema**: `public`

---

## Features

### Authentication & Security

- **JWT Authorization**: Token-based security signed by Spring Boot backend with role claim and session expiration.
- **BCrypt Password Security**: Salting and hash verification for user authentication.
- **Protected Routes**: Client-side route guards ensuring unauthenticated visitors are redirected to login.
- **Global Interceptors**: Automatic bearer token injection on outgoing Axios requests with instant 401 redirect handling.

### Operations Dashboard

- **Live KPI Analytics**: Aggregated real-time metrics for Total Vendors, Total Products, Total Purchase Orders, and Inventory Stock.
- **Pending & Low-Stock Alerts**: Visual counters highlighting orders awaiting approval and items requiring reorders.
- **Recent PO Activity**: Tabular overview of recent procurement orders with status badges (`Pending`, `Approved`, `Completed`, `Rejected`).
- **Inventory Stock Summary**: Real-time snapshot of product quantities and reorder thresholds.

### Core Enterprise Modules

- **Vendors Management**: Supplier directory tracking company details, contact persons, emails, phones, GST numbers, and active status (`GET /api/vendors`).
- **Products Catalog**: Comprehensive product list mapped to vendors with unit prices, units of measurement, and availability status (`GET /api/products`).
- **Purchase Orders**: Lifecycle tracking of orders including order dates, expected delivery dates, total amounts, item lines, and status badges (`GET /api/purchase-orders`, `POST /api/purchase-orders`, `PATCH /api/purchase-orders/{id}/status`).
- **Inventory Tracking**: Stock monitoring with automated status calculation (`In Stock`, `Low Stock`, `Reorder Required`) (`GET /api/inventory`).
- **Goods Receipts**: Delivery verification system linking received items to purchase orders and receiving personnel (`GET /api/goods-receipts`).

---

## Screenshots

> Note: UI screenshots will be captured and documented in the repository for Review-II.

---

## Getting Started

Follow these steps to set up and run POMS locally:

### 1. Clone Repository

```bash
git clone https://github.com/nishanthpn006/purchase-order-management-system.git
cd purchase-order-management-system
```

### 2. Database Setup (PostgreSQL)

Create the `purchase_order_db` database in PostgreSQL and initialize the schema and seed scripts:

```bash
psql -U postgres -d purchase_order_db -f database/schema.sql
psql -U postgres -d purchase_order_db -f database/seed.sql
```

### 3. Backend Setup (Spring Boot)

The application secures database credentials and JWT signing keys using environment variables. Set the required variables in your active terminal before starting the backend:

#### Windows PowerShell:

```powershell
# Set required secrets (no fallback defaults in configuration)
$env:SPRING_DATASOURCE_PASSWORD = "your_postgres_password"
$env:JWT_SECRET = "your_secure_jwt_signing_key_at_least_256_bits"

# Optional overrides (defaults to localhost:5432 and http://localhost:5173)
# $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/purchase_order_db?currentSchema=public"
# $env:SPRING_DATASOURCE_USERNAME = "postgres"
# $env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"

# Run Spring Boot backend:
cd backend
.\mvnw.cmd spring-boot:run
```

#### Linux / macOS Bash:

```bash
# Set required secrets
export SPRING_DATASOURCE_PASSWORD="your_postgres_password"
export JWT_SECRET="your_secure_jwt_signing_key_at_least_256_bits"

# Run Spring Boot backend:
cd backend
./mvnw spring-boot:run
```

The backend server runs on `http://localhost:5000`. Swagger documentation is available at `http://localhost:5000/swagger-ui.html`.

### 4. Frontend Setup (React + Vite)

In a new terminal window, navigate to the `frontend/` directory, install dependencies, and start the Vite dev server:

```bash
cd frontend
npm install
npm run dev
```

### 5. Access Application

- **Local Development**: [http://localhost:5173](http://localhost:5173)
- **Live Production**: [https://purchase-order-management-system-psi.vercel.app/](https://purchase-order-management-system-psi.vercel.app/)

#### User Roles & Access Overview

Authentication uses JWT Bearer tokens with role-based access control:

- **Administrator (`Admin`)**: Full application management permissions, subject to the configured endpoint security rules.
- **Procurement Manager (`Manager`)**: Operational procurement management (Vendors, Products, Purchase Orders, and Goods Receipts).
- **Employee (`Employee`)**: Procurement and catalog visibility, Goods Receipt entry (`POST /api/goods-receipts`). Authorized at the backend API layer to create purchase orders (`POST /api/purchase-orders`), but restricted from editing, cancelling, or changing purchase order status. *(Note: In the current frontend UI, the "Create Purchase Order" button is hidden from Employees via `canManagePurchaseOrders`.)*

---

## Configuration

The backend configuration is managed via `backend/src/main/resources/application.properties` and environment variables. See `.env.example` for a complete environment template.

| Property Key | Environment Variable | Default / Fallback | Description |
| :--- | :--- | :--- | :--- |
| `server.port` | `SERVER_PORT` / `PORT` | `5000` | HTTP port for REST API |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/purchase_order_db?currentSchema=public` | PostgreSQL JDBC connection URL |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `postgres` | Database user |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | *(None — Required)* | Database user password |
| `jwt.secret` | `JWT_SECRET` | *(None — Required)* | HMAC-SHA256 signing secret key (minimum 256 bits) |
| `jwt.expiration` | `JWT_EXPIRATION` | `86400000` (24h) | Token validity period in milliseconds |
| `cors.allowed-origins` | `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated list of allowed client origins |

---

## API Documentation

Interactive Swagger OpenAPI documentation is integrated directly into the Spring Boot backend for local development and testing:

- **Local Swagger UI**: [http://localhost:5000/swagger-ui.html](http://localhost:5000/swagger-ui.html)
- **Local OpenAPI JSON Spec**: [http://localhost:5000/v3/api-docs](http://localhost:5000/v3/api-docs)

> **Security Note**: Swagger UI and OpenAPI documentation endpoints are enabled only in local development profiles. In production (`spring.profiles.active=prod`), these endpoints are explicitly disabled via `application-prod.properties` for security hardening.

### Core REST Endpoints & Authorization Matrix

The table below delineates backend Spring Security authorization rules alongside frontend UI action visibility:

| Method | Endpoint | Backend Authorization | Authorized Roles | Frontend UI Visibility / Action Guard |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/login` | `permitAll()` | Public | Public login interface |
| `GET` | `/api/health` | `permitAll()` | Public | Automated monitoring |
| `GET` | `/api/me` | `authenticated()` | Admin, Manager, Employee | Session profile synchronization |
| `GET` | `/api/dashboard/stats` | `authenticated()` | Admin, Manager, Employee | Visible on Dashboard |
| `GET` | `/api/vendors` | `authenticated()` | Admin, Manager, Employee | Vendors directory list view |
| `GET` | `/api/vendors/{id}` | `authenticated()` | Admin, Manager, Employee | Vendor detail view |
| `POST` | `/api/vendors` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Add Vendor modal (`canManageVendors`) |
| `PUT` | `/api/vendors/{id}` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Edit Vendor modal (`canManageVendors`) |
| `PATCH` | `/api/vendors/{id}/deactivate` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Deactivate Vendor button (`canManageVendors`) |
| `GET` | `/api/products` | `authenticated()` | Admin, Manager, Employee | Products catalog list view |
| `GET` | `/api/products/{id}` | `authenticated()` | Admin, Manager, Employee | Product detail view |
| `POST` | `/api/products` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Add Product modal (`canManageProducts`) |
| `PUT` | `/api/products/{id}` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Edit Product modal (`canManageProducts`) |
| `PATCH` | `/api/products/{id}/deactivate` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Deactivate Product button (`canManageProducts`) |
| `GET` | `/api/purchase-orders` | `authenticated()` | Admin, Manager, Employee | Purchase orders list view |
| `GET` | `/api/purchase-orders/{id}` | `authenticated()` | Admin, Manager, Employee | Purchase order itemized detail view |
| `GET` | `/api/purchase-orders/{id}/receiving-details` | `authenticated()` | Admin, Manager, Employee | PO item delivery balance view |
| `POST` | `/api/purchase-orders` | `authenticated()` | Admin, Manager, Employee | Create PO button *(Admin & Manager in current UI)* |
| `PUT` | `/api/purchase-orders/{id}` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Edit PO action (`canManagePurchaseOrders`) |
| `PATCH` | `/api/purchase-orders/{id}/cancel` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Cancel PO action (`canManagePurchaseOrders`) |
| `PATCH` | `/api/purchase-orders/{id}/status` | `hasAnyRole("ADMIN", "MANAGER")` | Admin, Manager | Update status action (`canManagePurchaseOrders`) |
| `GET` | `/api/inventory` | `authenticated()` | Admin, Manager, Employee | Inventory monitoring table |
| `GET` | `/api/inventory/{id}` | `authenticated()` | Admin, Manager, Employee | Inventory item detail view |
| `GET` | `/api/goods-receipts` | `authenticated()` | Admin, Manager, Employee | Goods receipts delivery log |
| `GET` | `/api/goods-receipts/{id}` | `authenticated()` | Admin, Manager, Employee | Goods receipt detail view |
| `POST` | `/api/goods-receipts` | `hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")` | Admin, Manager, Employee | Receive Items action modal (`canReceiveGoods`) |

---

## Running Tests

Automated testing is implemented for both the backend service layer and frontend components:

### Backend Tests (JUnit 5 & Mockito)

```bash
cd backend
./mvnw test
# Or full clean verify:
./mvnw clean verify
```

### Frontend Lint & Build

```bash
cd frontend
npm run lint
npm run build
```

---

## Deployment

POMS is deployed across modern cloud infrastructure:

- **Frontend Client**: React 19 + Vite hosted on **Vercel** with automatic client-side route rewrites.
- **Backend API**: Spring Boot 3 + Java 21 containerized on **Render** (Docker runtime, binding to dynamic `$PORT`).
- **Relational Database**: Managed **PostgreSQL 15+ on Neon** with secure TLS connections and connection pooling.
- **Continuous Integration**: GitHub Actions workflow (`.github/workflows/ci.yml`) automatically builds and tests the backend with a live PostgreSQL 17 service container, executes frontend build/lint checks, and validates the backend Docker image.

---

## Folder Structure

```text
purchase-order-management-system/
├── backend/                  # Spring Boot 3 + Java 21 REST API
│   ├── .mvn/                # Maven wrapper binaries & properties
│   ├── src/
│   │   ├── main/java/com/poms/backend/
│   │   │   ├── config/      # Swagger OpenAPI configuration
│   │   │   ├── controller/  # REST controllers
│   │   │   ├── dto/         # Request & response DTOs
│   │   │   ├── entity/      # JPA entities
│   │   │   ├── repository/  # Spring Data JPA repositories
│   │   │   ├── security/    # JWT filter, provider, security configuration
│   │   │   └── service/     # Business logic services
│   │   ├── main/resources/  # application.properties
│   │   └── test/java/com/poms/backend/ # JUnit 5 + Mockito service test suite
│   ├── mvnw / mvnw.cmd      # Maven wrapper executable scripts
│   └── pom.xml              # Maven dependency descriptor
├── frontend/                 # React 19 + Vite web client
│   ├── src/
│   │   ├── components/      # Reusable UI components (Sidebar, Navbar, Badges)
│   │   ├── context/         # AuthContext & useAuth custom hook
│   │   ├── pages/           # Application views (Dashboard, Vendors, POs, etc.)
│   │   ├── services/        # Axios API client & endpoints
│   │   ├── styles/          # Enterprise CSS design system (poms.css)
│   │   ├── App.jsx          # Route configuration
│   │   └── main.jsx         # React application root
│   └── package.json
├── database/                 # Database schema & seed SQL scripts
├── diagrams/                 # System architecture & ER diagrams
├── docs/                     # Capstone documentation
├── CHANGELOG.md              # Project changelog
├── LICENSE                   # MIT License
└── README.md                 # Project documentation
```

---

## Future Enhancements

- **PDF Purchase Order Export**: Automated generation of branded, downloadable PDF documents for purchase orders.
- **Audit Logging Table**: Dedicated activity audit trail tracking critical entity changes and user timestamps.
- **Email Notifications**: Automated email alerts for pending approvals, purchase order status transitions, and low stock thresholds.
- **Multi-Currency Support**: Support for international procurement contracts with exchange rate conversions.

---

## License

This project is licensed under the **MIT License**.

---

## Author & Contact

**Nishanth P N**  
Pre-Final Year B.Tech Information Technology Student  
J. J. College of Engineering and Technology
