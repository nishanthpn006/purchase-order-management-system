CREATE DATABASE purchase_order_db;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table vendors(
id serial primary key,
vendor_name varchar(150) not null,
contact_person varchar(100),
email varchar(100),
phone varchar(20),
address text,
gst_number varchar(30),
status varchar(20) default 'active',
created_at timestamp default current_timestamp
);

create table products (
id serial primary key,
vendor_id int not null,
product_name varchar(150) not null,
category varchar(100),
description text,
unit_price decimal(10,2) not null,
stock_quantity int default 0,
unit varchar(30),
status varchar(20) default 'Available',
created_at timestamp default current_timestamp,
foreign key (vendor_id) references vendors(id)
);

create table purchase_orders (
id serial primary key,
po_number varchar(30) unique not null,
vendor_id int not null,
order_date date not null,
expected_delivery date,
total_amount decimal(12,2) default 0.00,
status varchar(20) default 'Pending',
created_by int,
created_at timestamp default current_timestamp,
foreign key (vendor_id) references vendors(id),
foreign key (created_by) references users(id)
);

create table purchase_order_items (
id serial,
purchase_order_id int not null,
product_id int not null,
quantity int not null,
unit_price decimal(10,2) not null,
total_price decimal(12,2) generated always as (quantity * unit_price) stored,
primary key (purchase_order_id, product_id),
foreign key (purchase_order_id) references purchase_orders(id),
foreign key (product_id) references products(id)
);

create table inventory (
id serial primary key,
product_id int unique not null,
quantity_in_stock int default 0,
reorder_level int default 10,
last_updated timestamp default current_timestamp,
foreign key (product_id) references products(id)
);

create table goods_receipts (
id serial primary key,
purchase_order_id int not null,
received_date date not null,
received_by int not null,
remarks varchar(255),
created_at timestamp default current_timestamp,
foreign key (purchase_order_id) references purchase_orders(id),
foreign key (received_by) references users(id)
);

