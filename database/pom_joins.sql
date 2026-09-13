select purchase_orders.po_number, vendors.vendor_name
from purchase_orders
inner join vendors
on purchase_orders.vendor_id = vendors.id;

select vendors.vendor_name, purchase_orders.po_number
from vendors
left join purchase_orders
on vendors.id = purchase_orders.vendor_id;

select vendors.vendor_name, purchase_orders.po_number
from vendors
right join purchase_orders
on vendors.id = purchase_orders.vendor_id;

select vendors.vendor_name, purchase_orders.po_number
from vendors
full join purchase_orders
on vendors.id = purchase_orders.vendor_id;

select products.product_name, vendors.vendor_name
from products
join vendors
on products.vendor_id = vendors.id;