const express = require("express");
const router = express.Router();
const { getAllPurchaseOrders } = require("../controllers/purchaseOrderController");
const { verifyToken } = require("../middlewares/authMiddleware");

router.get("/", verifyToken, getAllPurchaseOrders);

module.exports = router;
