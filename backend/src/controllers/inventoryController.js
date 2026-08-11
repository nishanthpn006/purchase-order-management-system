const db = require("../config/db");

// GET /api/inventory
const getAllInventory = async (req, res) => {
    try {
        const [rows] = await db.execute(
            `SELECT inv.id, inv.quantity_in_stock, inv.reorder_level, inv.last_updated,
                    p.product_name, p.category, p.unit,
                    v.vendor_name
             FROM inventory inv
             JOIN products p ON inv.product_id = p.id
             LEFT JOIN vendors v ON p.vendor_id = v.id
             ORDER BY p.product_name ASC`
        );
        return res.status(200).json({ success: true, data: rows });
    } catch (err) {
        console.error("Inventory error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load inventory." });
    }
};

module.exports = { getAllInventory };
