-- ============================================================
-- Purchase Order Management System (POMS) - Database Schema
-- Database Engine: PostgreSQL 15+
-- Database Name: purchase_order_db
-- ============================================================

-- ------------------------------------------------------------
-- Table: users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Table: vendors
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vendors (
    id SERIAL PRIMARY KEY,
    vendor_name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    gst_number VARCHAR(30),
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Table: products
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    vendor_id INT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    unit_price NUMERIC(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    unit VARCHAR(30),
    status VARCHAR(20) DEFAULT 'Available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT products_vendor_id_fkey FOREIGN KEY (vendor_id)
        REFERENCES vendors(id) ON DELETE CASCADE,
    CONSTRAINT chk_product_stock_non_negative CHECK (stock_quantity >= 0)
);

-- ------------------------------------------------------------
-- Table: purchase_orders
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_orders (
    id SERIAL PRIMARY KEY,
    po_number VARCHAR(30) UNIQUE NOT NULL,
    vendor_id INT NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery DATE,
    total_amount NUMERIC(12,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'Pending',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT purchase_orders_vendor_id_fkey FOREIGN KEY (vendor_id)
        REFERENCES vendors(id),
    CONSTRAINT purchase_orders_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Table: purchase_order_items (JPA Composite Key)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id SERIAL,
    purchase_order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    total_price NUMERIC(12,2) GENERATED ALWAYS AS ((quantity::NUMERIC * unit_price)) STORED,
    PRIMARY KEY (purchase_order_id, product_id),
    CONSTRAINT purchase_order_items_purchase_order_id_fkey FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT purchase_order_items_product_id_fkey FOREIGN KEY (product_id)
        REFERENCES products(id)
);

-- ------------------------------------------------------------
-- Table: inventory
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    id SERIAL PRIMARY KEY,
    product_id INT UNIQUE NOT NULL,
    quantity_in_stock INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT inventory_product_id_fkey FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_qty_non_negative CHECK (quantity_in_stock >= 0)
);

-- ------------------------------------------------------------
-- Table: goods_receipts (Header Table)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS goods_receipts (
    id SERIAL PRIMARY KEY,
    gr_number VARCHAR(30) UNIQUE NOT NULL,
    purchase_order_id INT NOT NULL,
    received_date DATE NOT NULL,
    received_by INT NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT goods_receipts_purchase_order_id_fkey FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id),
    CONSTRAINT goods_receipts_received_by_fkey FOREIGN KEY (received_by)
        REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Table: goods_receipt_items (Line Items - Source of Truth)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS goods_receipt_items (
    id SERIAL PRIMARY KEY,
    goods_receipt_id INT NOT NULL,
    product_id INT NOT NULL,
    received_quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gri_goods_receipt FOREIGN KEY (goods_receipt_id)
        REFERENCES goods_receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_gri_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT uq_gri_receipt_product UNIQUE (goods_receipt_id, product_id),
    CONSTRAINT chk_gri_received_qty_positive CHECK (received_quantity > 0)
);

-- ------------------------------------------------------------
-- Indexes for Efficient Lookups
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_goods_receipts_po_id
    ON goods_receipts(purchase_order_id);

CREATE INDEX IF NOT EXISTS idx_goods_receipts_date
    ON goods_receipts(received_date DESC);

CREATE INDEX IF NOT EXISTS idx_gri_receipt_id
    ON goods_receipt_items(goods_receipt_id);

CREATE INDEX IF NOT EXISTS idx_gri_product_id
    ON goods_receipt_items(product_id);
