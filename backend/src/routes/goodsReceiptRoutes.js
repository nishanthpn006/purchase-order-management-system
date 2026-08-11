const express = require("express");
const router = express.Router();
const { getAllGoodsReceipts } = require("../controllers/goodsReceiptController");
const { verifyToken } = require("../middlewares/authMiddleware");

router.get("/", verifyToken, getAllGoodsReceipts);

module.exports = router;
