const express = require("express");
const router = express.Router();
const { getAllInventory } = require("../controllers/inventoryController");
const { verifyToken } = require("../middlewares/authMiddleware");

router.get("/", verifyToken, getAllInventory);

module.exports = router;
