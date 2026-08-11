const express = require("express");
const cors = require("cors");
require("dotenv").config();

const db = require("./config/db");
const authRoutes = require("./routes/authRoutes");
const dashboardRoutes = require("./routes/dashboardRoutes");
const vendorRoutes = require("./routes/vendorRoutes");
const productRoutes = require("./routes/productRoutes");
const purchaseOrderRoutes = require("./routes/purchaseOrderRoutes");
const inventoryRoutes = require("./routes/inventoryRoutes");
const goodsReceiptRoutes = require("./routes/goodsReceiptRoutes");

const app = express();

const corsOptions = {
    origin: process.env.FRONTEND_URL || "http://localhost:5173",
    credentials: true,
};

app.use(cors(corsOptions));
app.use(express.json());

// ── Health ───────────────────────────────────────────────────────────────────
app.get("/", (req, res) => res.send("Purchase Order Management System API"));
app.get("/api/health", (req, res) =>
    res.json({ status: "OK", message: "POMS API is running." })
);

// ── Routes ───────────────────────────────────────────────────────────────────
app.use("/api", authRoutes);
app.use("/api/dashboard", dashboardRoutes);
app.use("/api/vendors", vendorRoutes);
app.use("/api/products", productRoutes);
app.use("/api/purchase-orders", purchaseOrderRoutes);
app.use("/api/inventory", inventoryRoutes);
app.use("/api/goods-receipts", goodsReceiptRoutes);

// ── Start ─────────────────────────────────────────────────────────────────────
const PORT = process.env.PORT || 5000;

async function testDB() {
    try {
        const connection = await db.getConnection();
        console.log("✅ MySQL Connected Successfully");
        connection.release();
    } catch (err) {
        console.error("❌ MySQL Connection Failed:", err.message);
    }
}

testDB();
app.listen(PORT, () => console.log(`🚀 Server running on http://localhost:${PORT}`));