const db = require("../config/db");

// GET /api/dashboard/stats
const getStats = async (req, res) => {
    try {
        const [[{ total_vendors }]] = await db.execute("SELECT COUNT(*) AS total_vendors FROM vendors");
        const [[{ total_products }]] = await db.execute("SELECT COUNT(*) AS total_products FROM products");
        const [[{ total_orders }]] = await db.execute("SELECT COUNT(*) AS total_orders FROM purchase_orders");
        const [[{ total_inventory }]] = await db.execute("SELECT COUNT(*) AS total_inventory FROM inventory");
        const [[{ pending_orders }]] = await db.execute("SELECT COUNT(*) AS pending_orders FROM purchase_orders WHERE status = 'Pending'");
        const [[{ low_stock }]] = await db.execute("SELECT COUNT(*) AS low_stock FROM inventory WHERE quantity_in_stock <= reorder_level");

        return res.status(200).json({
            success: true,
            data: { total_vendors, total_products, total_orders, total_inventory, pending_orders, low_stock },
        });
    } catch (err) {
        console.error("Dashboard stats error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load dashboard statistics." });
    }
};

module.exports = { getStats };
