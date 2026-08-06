const express = require("express");
const cors = require("cors");
require("dotenv").config();

const authRoutes = require("./routes/authRoutes");
const db = require("./config/db");
const app = express();

app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
    res.send("Purchase Order Management System API");
});

app.get("/api/health", (req, res) => {
    res.json({
        status: "OK",
        message: "Purchase Order Management System API is running."
    });
});

app.use("/api", authRoutes);

const PORT = process.env.PORT || 5000;
async function testDB() {
    try {
        const connection = await db.getConnection();
        console.log("✅ MySQL Connected Successfully");
        connection.release();
    } catch (err) {
        console.error("❌ MySQL Connection Failed");
        console.error(err.message);
    }
}

testDB();
app.listen(PORT, () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
});