const express = require("express");
const router = express.Router();

const { login, getMe } = require("../controllers/authController");
const { verifyToken } = require("../middlewares/authMiddleware");

// Public routes
router.post("/login", login);

// Protected routes (require valid JWT)
router.get("/me", verifyToken, getMe);

module.exports = router;