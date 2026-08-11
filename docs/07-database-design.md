# Database Design

## 1. Introduction

The Purchase Order Management System (POMS) uses a MySQL 8 relational database (`purchase_order_db`). The database design enforces data integrity, foreign key constraints, unique indexing, and auto-incrementing primary keys across 7 core entities.

---

## 2. Database Overview

- **Engine**: MySQL 8.0+
- **Database Name**: `purchase_order_db`
- **Character Set**: `utf8mb4`
- **Collation**: `utf8mb4_0900_ai_ci`
- **Driver**: `mysql2/promise` (Node.js connection pool)
- **Schema File**: `database/schema.sql`
- **Seed File**: `database/seed.sql`

---

## 3. Detailed Entity Schema

### 1. `users` Table

Stores user account records, credentials, and roles.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `full_name` (VARCHAR(100), NOT NULL): User's full name
- `email` (VARCHAR(100), NOT NULL, UNIQUE): Account email / login identity
- `password` (VARCHAR(255), NOT NULL): Salted bcrypt password hash
- `role` (ENUM('Admin', 'Manager', 'Employee'), NOT NULL): User authorization level
- `status` (ENUM('Active', 'Inactive'), DEFAULT 'Active'): Account state
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Registration timestamp

### 2. `vendors` Table

Stores registered supplier profiles and contact data.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `vendor_name` (VARCHAR(150), NOT NULL): Company name
- `contact_person` (VARCHAR(100), NULL): Primary contact name
- `email` (VARCHAR(100), NULL): Contact email
- `phone` (VARCHAR(20), NULL): Contact phone number
- `address` (TEXT, NULL): Physical address
- `gst_number` (VARCHAR(30), NULL): GST tax identification number
- `status` (ENUM('Active', 'Inactive'), DEFAULT 'Active'): Vendor operational status
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Creation timestamp

### 3. `products` Table

Stores catalog items supplied by vendors.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `vendor_id` (INT, NOT NULL, FK -> `vendors.id`): Associated vendor ID
- `product_name` (VARCHAR(150), NOT NULL): Item name
- `category` (VARCHAR(100), NULL): Product category
- `description` (TEXT, NULL): Item specifications
- `unit_price` (DECIMAL(10,2), NOT NULL): Default unit cost
- `stock_quantity` (INT, DEFAULT 0): Catalog stock quantity
- `unit` (VARCHAR(30), NULL): Unit of measurement (e.g., Piece, Box)
- `status` (ENUM('Available', 'Unavailable'), DEFAULT 'Available'): Catalog availability
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Creation timestamp

### 4. `purchase_orders` Table

Stores purchase order headers.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `po_number` (VARCHAR(30), NOT NULL, UNIQUE): Human-readable order identifier (e.g., `PO1001`)
- `vendor_id` (INT, NOT NULL, FK -> `vendors.id`): Selected supplier
- `order_date` (DATE, NOT NULL): Order creation date
- `expected_delivery` (DATE, NULL): Target delivery date
- `total_amount` (DECIMAL(12,2), DEFAULT 0.00): Aggregated order cost
- `status` (ENUM('Pending', 'Approved', 'Rejected', 'Completed'), DEFAULT 'Pending'): Order lifecycle state
- `created_by` (INT, NULL, FK -> `users.id`): Purchasing user ID
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Record timestamp

### 5. `purchase_order_items` Table

Stores line items associated with purchase orders.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `purchase_order_id` (INT, NOT NULL, FK -> `purchase_orders.id` ON DELETE CASCADE): Order header
- `product_id` (INT, NOT NULL, FK -> `products.id`): Ordered catalog product
- `quantity` (INT, NOT NULL): Ordered quantity
- `unit_price` (DECIMAL(10,2), NOT NULL): Unit price at purchase
- `total_price` (DECIMAL(12,2), GENERATED STORED AS `quantity * unit_price`): Computed item total

### 6. `inventory` Table

Monitors warehouse stock and reorder thresholds.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `product_id` (INT, NOT NULL, UNIQUE, FK -> `products.id` ON DELETE CASCADE): Product reference
- `quantity_in_stock` (INT, DEFAULT 0): Current warehouse stock
- `reorder_level` (INT, DEFAULT 10): Minimum threshold before reorder alert
- `last_updated` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP): Last stock update

### 7. `goods_receipts` Table

Tracks delivery verification receipts.

- `id` (INT, PK, AUTO_INCREMENT): Primary key
- `purchase_order_id` (INT, NOT NULL, FK -> `purchase_orders.id`): Delivered purchase order
- `received_date` (DATE, NOT NULL): Date items were delivered
- `received_by` (INT, NOT NULL, FK -> `users.id`): Receiving personnel user ID
- `remarks` (VARCHAR(255), NULL): Inspection remarks or notes
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Record creation timestamp

---

## 4. Foreign Key Relational Summary

| Child Table | Foreign Key Field | Parent Table | Parent Key | Constraint Rule |
| --- | --- | --- | --- | --- |
| `products` | `vendor_id` | `vendors` | `id` | ON DELETE CASCADE |
| `purchase_orders` | `vendor_id` | `vendors` | `id` | RESTRICT |
| `purchase_orders` | `created_by` | `users` | `id` | RESTRICT |
| `purchase_order_items` | `purchase_order_id` | `purchase_orders` | `id` | ON DELETE CASCADE |
| `purchase_order_items` | `product_id` | `products` | `id` | RESTRICT |
| `inventory` | `product_id` | `products` | `id` | ON DELETE CASCADE |
| `goods_receipts` | `purchase_order_id` | `purchase_orders` | `id` | RESTRICT |
| `goods_receipts` | `received_by` | `users` | `id` | RESTRICT |