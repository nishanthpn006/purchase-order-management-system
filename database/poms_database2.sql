insert into users (full_name, email, password, role, status)
values
('Nishanth PN', 'nishanth@poms.com', '$2a$10$i1B0zTZJFi/tKd4rkC.RI.wsjAXX7neoq9mtUwlx9P408XRtauwUG', 'Admin', 'Active'),
('Arun Kumar', 'arun@poms.com', '$2a$10$TplK03kewWU.8g2hlmBPiOCKb4wgBTNBBLSScD29gww9c6M8TBH/C', 'Manager', 'Active'),
('Priya Sharma', 'priya@poms.com', '$2a$10$saWYiUl5VVPP3mlIJxNPYepswtPtLQAF0Eaqi5QKJMlkFml3JnPpi', 'Employee', 'Active'),
('Rahul Das', 'rahul@poms.com', '$2a$10$5LkSSluBsxw1Yh6H9oCqY.2uv8LDLiQi8TEFL4lmEb5hNYsZFf0Ui', 'Employee', 'Active'),
('Sneha R', 'sneha@poms.com', '$2a$10$MJ3m6.NYdFgEUAyJSZEskuCzbcX5bS3ogtzbYpQ0bL8RbbUKxyEKC', 'Manager', 'Active');

select id, full_name from users;

insert into purchase_orders (po_number, vendor_id, order_date, expected_delivery, total_amount, status, created_by)
values
('PO1001', 1, '2026-09-01', '2026-09-10', 60000.00, 'Pending', 1),
('PO1002', 2, '2026-09-02', '2026-09-12', 90000.00, 'Approved', 2),
('PO1003', 3, '2026-09-03', '2026-09-13', 110000.00, 'Completed', 1),
('PO1004', 4, '2026-09-04', '2026-09-15', 96000.00, 'Pending', 3),
('PO1005', 5, '2026-09-05', '2026-09-16', 25000.00, 'Approved', 2);

select id, po_number from purchase_orders;

insert into purchase_order_items (purchase_order_id, product_id, quantity, unit_price)
values
(26, 1, 5, 12000.00),
(27, 2, 5, 18000.00),
(28, 3, 2, 55000.00),
(29, 4, 2, 48000.00),
(30, 5, 10, 2500.00);

select purchase_order_id, product_id, quantity, unit_price, total_price
from purchase_order_items;

insert into inventory (product_id, quantity_in_stock, reorder_level)
values
(1, 20, 5),
(2, 15, 5),
(3, 10, 3),
(4, 12, 3),
(5, 30, 10);

select id, product_id, quantity_in_stock from inventory;

insert into goods_receipts (purchase_order_id, received_date, received_by, remarks)
values
(26, '2026-09-10', 1, 'Received in good condition'),
(27, '2026-09-12', 2, 'All items received'),
(28, '2026-09-13', 1, 'Received and verified'),
(29, '2026-09-15', 3, 'Items received successfully'),
(30, '2026-09-16', 2, 'Received without damage');

select id, purchase_order_id, received_date, received_by from goods_receipts;


insert into products (vendor_id, product_name, category, unit_price)
values (99, 'Test Product', 'Test', 1000);

