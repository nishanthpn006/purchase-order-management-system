const db = require("../config/db");

// GET /api/products
const getAllProducts = async (req, res) => {
    try {
        const [rows] = await db.execute(
            `SELECT p.id, p.product_name, p.category, p.description, p.unit_price,
                    p.stock_quantity, p.unit, p.status, p.created_at,
                    v.vendor_name
             FROM products p
             LEFT JOIN vendors v ON p.vendor_id = v.id
             ORDER BY p.product_name ASC`
        );
        return res.status(200).json({ success: true, data: rows });
    } catch (err) {
        console.error("Products error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load products." });
    }
};

module.exports = { getAllProducts };
