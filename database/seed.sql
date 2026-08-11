-- ============================================================
-- Purchase Order Management System (POMS) - Seed Data
-- Database Engine: MySQL 8
-- Database Name: purchase_order_db
-- ============================================================

USE `purchase_order_db`;

-- Disable foreign key checks during seeding
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE `goods_receipts`;
TRUNCATE TABLE `purchase_order_items`;
TRUNCATE TABLE `inventory`;
TRUNCATE TABLE `purchase_orders`;
TRUNCATE TABLE `products`;
TRUNCATE TABLE `vendors`;
TRUNCATE TABLE `users`;

-- ------------------------------------------------------------
-- Seed Data: users
-- Password: admin123 (stored as bcrypt hash)
-- ------------------------------------------------------------
INSERT INTO `users` (`id`, `full_name`, `email`, `password`, `role`, `status`, `created_at`) VALUES
(1, 'Administrator', 'admin@poms.com', '$2b$10$QNgY2fyRPalFUEbmozJ53ODZ.EewvlHiCXmfRZAJWa9AEOhAs0dXS', 'Admin', 'Active', '2026-08-06 15:42:49');

-- ------------------------------------------------------------
-- Seed Data: vendors
-- ------------------------------------------------------------
INSERT INTO `vendors` (`id`, `vendor_name`, `contact_person`, `email`, `phone`, `address`, `gst_number`, `status`, `created_at`) VALUES
(1, 'Dell Technologies', 'John Smith', 'dell@vendor.com', '9876543210', 'Bangalore', '29ABCDE1234F1Z5', 'Active', '2026-08-06 15:47:05'),
(2, 'HP India', 'David Wilson', 'hp@vendor.com', '9876543211', 'Chennai', '33ABCDE5678F1Z5', 'Active', '2026-08-06 15:47:05'),
(3, 'Lenovo India', 'Robert James', 'lenovo@vendor.com', '9876543212', 'Hyderabad', '36ABCDE9101F1Z5', 'Active', '2026-08-06 15:47:05');

-- ------------------------------------------------------------
-- Seed Data: products
-- ------------------------------------------------------------
INSERT INTO `products` (`id`, `vendor_id`, `product_name`, `category`, `description`, `unit_price`, `stock_quantity`, `unit`, `status`, `created_at`) VALUES
(1, 1, 'Dell Latitude 5440', 'Laptop', '14 inch Business Laptop', 65000.00, 25, 'Piece', 'Available', '2026-08-06 15:49:18'),
(2, 2, 'HP LaserJet Pro', 'Printer', 'Laser Printer', 18000.00, 10, 'Piece', 'Available', '2026-08-06 15:49:18'),
(3, 3, 'Lenovo ThinkPad E14', 'Laptop', 'Business Laptop', 58000.00, 18, 'Piece', 'Available', '2026-08-06 15:49:18'),
(4, 1, 'Dell 24 Monitor', 'Monitor', '24 inch LED Monitor', 12000.00, 30, 'Piece', 'Available', '2026-08-06 15:49:18'),
(5, 2, 'HP Keyboard', 'Accessories', 'USB Keyboard', 900.00, 120, 'Piece', 'Available', '2026-08-06 15:49:18');

-- ------------------------------------------------------------
-- Seed Data: purchase_orders
-- ------------------------------------------------------------
INSERT INTO `purchase_orders` (`id`, `po_number`, `vendor_id`, `order_date`, `expected_delivery`, `total_amount`, `status`, `created_by`, `created_at`) VALUES
(1, 'PO1001', 1, '2026-08-06', '2026-08-10', 60000.00, 'Pending', 1, '2026-08-06 15:55:35'),
(2, 'PO1002', 2, '2026-08-05', '2026-08-09', 25000.00, 'Approved', 1, '2026-08-06 15:55:35'),
(3, 'PO1003', 3, '2026-08-04', '2026-08-08', 45000.00, 'Completed', 1, '2026-08-06 15:55:35');

-- ------------------------------------------------------------
-- Seed Data: purchase_order_items
-- Note: total_price is a STORED GENERATED column in MySQL
-- ------------------------------------------------------------
INSERT INTO `purchase_order_items` (`id`, `purchase_order_id`, `product_id`, `quantity`, `unit_price`) VALUES
(1, 1, 1, 1, 55000.00),
(2, 1, 2, 5, 12000.00),
(3, 2, 3, 10, 900.00),
(4, 3, 4, 20, 500.00),
(5, 3, 5, 5, 6000.00);

-- ------------------------------------------------------------
-- Seed Data: inventory
-- ------------------------------------------------------------
INSERT INTO `inventory` (`id`, `product_id`, `quantity_in_stock`, `reorder_level`, `last_updated`) VALUES
(1, 1, 20, 5, '2026-08-06 15:57:06'),
(2, 2, 50, 10, '2026-08-06 15:57:06'),
(3, 3, 100, 20, '2026-08-06 15:57:06'),
(4, 4, 200, 30, '2026-08-06 15:57:06'),
(5, 5, 40, 10, '2026-08-06 15:57:06');

-- ------------------------------------------------------------
-- Seed Data: goods_receipts
-- ------------------------------------------------------------
INSERT INTO `goods_receipts` (`id`, `purchase_order_id`, `received_date`, `received_by`, `remarks`, `created_at`) VALUES
(1, 3, '2026-08-08', 1, 'Items received in good condition', '2026-08-06 15:57:50');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
