# 📦 Purchase Order Management System (POMS)

An enterprise-grade procurement and purchase order management web application developed as a pre-final-year capstone project. POMS streamlines organizational procurement workflows by digitizing vendor tracking, product cataloging, purchase order lifecycles, stock monitoring, and delivery receipts.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Database Schema](#-database-schema)
- [System Architecture & Workflow](#-system-architecture--workflow)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [License](#-license)

---

## 🎯 Overview

Manual procurement workflows reliant on static spreadsheets and paper approvals lead to operational bottlenecks, misplaced orders, and untracked inventory levels. **POMS** addresses these challenges through a centralized, role-aware management system that connects procurement actions directly to live inventory and database records.

---

## ✨ Key Features

### 🔐 Authentication & Security
- **JWT Authorization**: Token-based security signed by Express backend with 8-hour session expiration.
- **Protected Routes**: React Router guards ensuring unauthenticated visitors are redirected to login.
- **Role Awareness**: Decoded user session containing role levels (`Admin`, `Manager`, `Employee`).
- **Global Interceptors**: Automatic bearer token injection on outgoing Axios requests with instant 401 redirect handling.

### 📊 Operations Dashboard
- **Live KPI Analytics**: Aggregated real-time metrics for Total Vendors, Total Products, Total Purchase Orders, and Inventory Stock.
- **Pending & Low-Stock Alerts**: Visual counters highlighting orders awaiting approval and items requiring reorders.
- **Recent PO Activity**: Tabular overview of recent procurement orders with status badges (`Pending`, `Approved`, `Completed`, `Rejected`).
- **Inventory Stock Summary**: Real-time snapshot of product quantities and reorder thresholds.

### 🏢 Core Enterprise Modules
- **Vendors Management**: Supplier directory tracking company details, contact persons, emails, phones, GST numbers, and active status.
- **Products Catalog**: Comprehensive product list mapped to vendors with unit prices, units of measurement, and availability status.
- **Purchase Orders**: Lifecycle tracking of orders including order dates, expected delivery dates, total amounts, and status badges.
- **Inventory Tracking**: Stock monitoring with automated status calculation (`In Stock`, `Low Stock`, `Reorder Required`).
- **Goods Receipts**: Delivery verification system linking received items to purchase orders and receiving personnel.

---

## 🛠 Tech Stack

### Frontend
- **Framework**: React 19 + Vite
- **Routing**: React Router 7
- **HTTP Client**: Axios with custom interceptors
- **Icons**: Lucide React
- **Styling**: Custom CSS Enterprise Design System with CSS variables and responsive breakpoints

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database Driver**: `mysql2/promise` (Connection Pooling)
- **Security**: `bcryptjs` for password verification, `jsonwebtoken` for auth tokens
- **Environment**: `dotenv`, CORS enabled

### Database
- **Engine**: MySQL 8
- **Database Name**: `purchase_order_db`

---

## 🗄 Database Schema

The system connects to `purchase_order_db` consisting of 7 relational tables:

```
+---------------+       +------------------+       +---------------+
|    users      |       |     vendors      |       |   products    |
+---------------+       +------------------+       +---------------+
| id (PK)       |       | id (PK)          |       | id (PK)       |
| full_name     |       | vendor_name      |       | vendor_id(FK) |
| email         |       | contact_person   |       | product_name  |
| password      |       | email            |       | category      |
| role          |       | phone            |       | unit_price    |
| status        |       | gst_number       |       | stock_quantity|
+---------------+       | status           |       | status        |
                        +------------------+       +---------------+
                                 |                         |
                                 v                         v
                        +------------------+       +---------------+
                        | purchase_orders  |       |   inventory   |
                        +------------------+       +---------------+
                        | id (PK)          |       | id (PK)       |
                        | po_number        |       | product_id(FK)|
                        | vendor_id (FK)   |       | qty_in_stock  |
                        | order_date       |       | reorder_level |
                        | expected_delivery|       | last_updated  |
                        | total_amount     |       +---------------+
                        | status           |
                        | created_by (FK)  |
                        +------------------+
                                 |
                                 v
                        +------------------+
                        |  goods_receipts  |
                        +------------------+
                        | id (PK)          |
                        | po_id (FK)       |
                        | received_date    |
                        | received_by (FK) |
                        | remarks          |
                        +------------------+
```

---

## 🚀 Getting Started

### Prerequisites
- Node.js (v18+)
- MySQL Server (v8.0+)
- npm or yarn

### 1. Database Setup
Import and execute the SQL schema in MySQL Workbench or terminal to create `purchase_order_db`:

```sql
CREATE DATABASE IF NOT EXISTS purchase_order_db;
USE purchase_order_db;
```

Ensure sample data for `users`, `vendors`, `products`, `purchase_orders`, `inventory`, and `goods_receipts` is populated.

### 2. Backend Installation & Setup
Navigate to the backend directory:

```bash
cd backend
npm install
```

Create a `.env` file inside `backend/`:

```env
PORT=5000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=purchase_order_db
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRES_IN=8h
```

Start the backend server:

```bash
node src/server.js
```

### 3. Frontend Installation & Setup
Navigate to the frontend directory:

```bash
cd ../frontend
npm install
```

Start the Vite development server:

```bash
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 📡 API Documentation

### Authentication
- `POST /api/login` - Authenticate user & return JWT token
- `GET /api/me` - Validate session & return decoded user info

### Operations & Dashboard
- `GET /api/dashboard/stats` - Returns aggregated KPI counts
- `GET /api/vendors` - List all registered vendors
- `GET /api/products` - List catalog products with vendor details
- `GET /api/purchase-orders` - List purchase orders with vendor and creator names
- `GET /api/inventory` - List inventory stock levels with product details
- `GET /api/goods-receipts` - List goods receipts and delivery records

---

## 📁 Project Structure

```
purchase-order-management-system/
├── backend/
│   ├── src/
│   │   ├── config/          # MySQL connection pool
│   │   ├── controllers/     # API business logic
│   │   ├── middlewares/     # JWT authentication middleware
│   │   ├── routes/          # Express route definitions
│   │   └── server.js        # Express app entrypoint
│   └── .env                 # Environment variables
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable UI components (Sidebar, Navbar, Badges)
│   │   ├── context/         # AuthContext & useAuth custom hook
│   │   ├── pages/           # Application pages (Dashboard, Vendors, POs, etc.)
│   │   ├── services/        # Axios API client & endpoints
│   │   ├── styles/          # Enterprise CSS design system (poms.css)
│   │   ├── App.jsx          # Route configuration
│   │   └── main.jsx         # React application root
│   └── vite.config.js       # Vite build configuration
└── README.md
```

---

## 📄 License

This project is licensed under the **MIT License**.
