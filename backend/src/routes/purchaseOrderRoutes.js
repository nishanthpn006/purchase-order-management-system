const express = require("express");
const router = express.Router();
const {
    getAllPurchaseOrders,
    getPurchaseOrderById,
    createPurchaseOrder,
    updatePurchaseOrderStatus,
} = require("../controllers/purchaseOrderController");
const { verifyToken, authorizeRoles } = require("../middlewares/authMiddleware");

// All routes require authentication
router.use(verifyToken);

// View purchase orders
router.get("/", getAllPurchaseOrders);
router.get("/:id", getPurchaseOrderById);

// Create purchase order (Admin, Manager, Employee)
router.post("/", authorizeRoles("Admin", "Manager", "Employee"), createPurchaseOrder);

// Update status / approval workflow (Admin, Manager)
router.patch("/:id/status", authorizeRoles("Admin", "Manager"), updatePurchaseOrderStatus);

module.exports = router;
