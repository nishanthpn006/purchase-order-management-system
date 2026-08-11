const express = require("express");
const router = express.Router();
const { getAllVendors } = require("../controllers/vendorController");
const { verifyToken } = require("../middlewares/authMiddleware");

router.get("/", verifyToken, getAllVendors);

module.exports = router;
