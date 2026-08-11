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

module.exports = { getAllPurchaseOrders };
