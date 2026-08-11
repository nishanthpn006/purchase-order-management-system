const db = require("../config/db");

// GET /api/vendors
const getAllVendors = async (req, res) => {
    try {
        const [rows] = await db.execute(
            "SELECT id, vendor_name, contact_person, email, phone, gst_number, status, created_at FROM vendors ORDER BY vendor_name ASC"
        );
        return res.status(200).json({ success: true, data: rows });
    } catch (err) {
        console.error("Vendors error:", err.message);
        return res.status(500).json({ success: false, message: "Failed to load vendors." });
    }
};

module.exports = { getAllVendors };
