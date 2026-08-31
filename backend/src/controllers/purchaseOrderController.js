const db = require("../config/db");

// GET /api/purchase-orders
const getAllPurchaseOrders = async (req, res) => {
    try {
        const [rows] = await db.execute(
            `SELECT po.id, po.po_number, po.order_date, po.expected_delivery,
                    po.total_amount, po.status, po.created_at,
                    v.vendor_name,
                    u.full_name AS created_by_name
             FROM purchase_orders po
             LEFT JOIN vendors v ON po.vendor_id = v.id
             LEFT JOIN users u ON po.created_by = u.id
             ORDER BY po.created_at DESC`
        );
        return res.status(200).json({ success: true, data: rows });
    } catch (err) {
        console.error("Purchase orders error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load purchase orders." });
    }
};

// GET /api/purchase-orders/:id
const getPurchaseOrderById = async (req, res) => {
    try {
        const { id } = req.params;
        const [poRows] = await db.execute(
            `SELECT po.id, po.po_number, po.vendor_id, po.order_date, po.expected_delivery,
                    po.total_amount, po.status, po.created_by, po.created_at,
                    v.vendor_name, v.contact_person, v.email AS vendor_email, v.phone AS vendor_phone,
                    u.full_name AS created_by_name
             FROM purchase_orders po
             LEFT JOIN vendors v ON po.vendor_id = v.id
             LEFT JOIN users u ON po.created_by = u.id
             WHERE po.id = ?`,
            [id]
        );

        if (poRows.length === 0) {
            return res.status(404).json({ success: false, message: "Purchase order not found." });
        }

        const [itemRows] = await db.execute(
            `SELECT poi.id, poi.purchase_order_id, poi.product_id, poi.quantity,
                    poi.unit_price, poi.total_price,
                    p.product_name, p.category, p.unit
             FROM purchase_order_items poi
             JOIN products p ON poi.product_id = p.id
             WHERE poi.purchase_order_id = ?`,
            [id]
        );

        const purchaseOrder = {
            ...poRows[0],
            items: itemRows,
        };

        return res.status(200).json({ success: true, data: purchaseOrder });
    } catch (err) {
        console.error("Get PO by ID error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load purchase order details." });
    }
};

// POST /api/purchase-orders
const createPurchaseOrder = async (req, res) => {
    const connection = await db.getConnection();
    try {
        const { vendor_id, expected_delivery, items } = req.body;
        const created_by = req.user ? req.user.id : null;

        if (!vendor_id || !Array.isArray(items) || items.length === 0) {
            return res.status(400).json({
                success: false,
                message: "Vendor ID and at least one item are required.",
            });
        }

        await connection.beginTransaction();

        // Generate unique PO number: PO-YYYYMMDD-XXXX
        const today = new Date();
        const dateStr = today.toISOString().slice(0, 10).replace(/-/g, "");
        const randomSuffix = Math.floor(1000 + Math.random() * 9000);
        const po_number = `PO-${dateStr}-${randomSuffix}`;
        const order_date = today.toISOString().slice(0, 10);

        // Calculate total amount & validate items
        let calculatedTotal = 0;
        for (const item of items) {
            if (!item.product_id || !item.quantity || !item.unit_price) {
                await connection.rollback();
                return res.status(400).json({
                    success: false,
                    message: "Each item must specify product_id, quantity, and unit_price.",
                });
            }
            calculatedTotal += Number(item.quantity) * Number(item.unit_price);
        }

        // Insert Purchase Order header
        const [poResult] = await connection.execute(
            `INSERT INTO purchase_orders (po_number, vendor_id, order_date, expected_delivery, total_amount, status, created_by)
             VALUES (?, ?, ?, ?, ?, 'Pending', ?)`,
            [po_number, vendor_id, order_date, expected_delivery || null, calculatedTotal, created_by]
        );

        const poId = poResult.insertId;

        // Insert line items
        for (const item of items) {
            await connection.execute(
                `INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_price)
                 VALUES (?, ?, ?, ?)`,
                [poId, item.product_id, item.quantity, item.unit_price]
            );
        }

        await connection.commit();

        return res.status(201).json({
            success: true,
            message: "Purchase order created successfully.",
            data: { id: poId, po_number, total_amount: calculatedTotal, status: "Pending" },
        });
    } catch (err) {
        await connection.rollback();
        console.error("Create PO error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to create purchase order." });
    } finally {
        connection.release();
    }
};

// PATCH /api/purchase-orders/:id/status
const updatePurchaseOrderStatus = async (req, res) => {
    try {
        const { id } = req.params;
        const { status } = req.body;

        const validStatuses = ["Pending", "Approved", "Rejected", "Completed"];
        if (!validStatuses.includes(status)) {
            return res.status(400).json({
                success: false,
                message: `Invalid status. Allowed statuses: ${validStatuses.join(", ")}`,
            });
        }

        const [result] = await db.execute(
            `UPDATE purchase_orders SET status = ? WHERE id = ?`,
            [status, id]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: "Purchase order not found." });
        }

        return res.status(200).json({
            success: true,
            message: `Purchase order status updated to ${status}.`,
            data: { id: Number(id), status },
        });
    } catch (err) {
        console.error("Update PO status error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to update purchase order status." });
    }
};

module.exports = {
    getAllPurchaseOrders,
    getPurchaseOrderById,
    createPurchaseOrder,
    updatePurchaseOrderStatus,
};
