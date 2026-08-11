const db = require("../config/db");

// GET /api/goods-receipts
const getAllGoodsReceipts = async (req, res) => {
    try {
        const [rows] = await db.execute(
            `SELECT gr.id, gr.received_date, gr.remarks, gr.created_at,
                    po.po_number,
                    v.vendor_name,
                    u.full_name AS received_by_name
             FROM goods_receipts gr
             JOIN purchase_orders po ON gr.purchase_order_id = po.id
             LEFT JOIN vendors v ON po.vendor_id = v.id
             LEFT JOIN users u ON gr.received_by = u.id
             ORDER BY gr.received_date DESC`
        );
        return res.status(200).json({ success: true, data: rows });
    } catch (err) {
        console.error("Goods receipts error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load goods receipts." });
    }
};

module.exports = { getAllGoodsReceipts };
