const express = require("express");
const router = express.Router();
const { getAllProducts } = require("../controllers/productController");
const { verifyToken } = require("../middlewares/authMiddleware");

router.get("/", verifyToken, getAllProducts);

module.exports = router;
