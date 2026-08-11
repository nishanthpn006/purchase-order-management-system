const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const db = require("../config/db");

// POST /api/login
const login = async (req, res) => {
    const { email, password } = req.body;

    // Basic input validation
    if (!email || !password) {
        return res.status(400).json({
            success: false,
            message: "Email and password are required.",
        });
    }

    try {
        // Query against actual schema: PK=id, password col=password, role=enum on users
        const [rows] = await db.execute(
            `SELECT id, full_name, email, password, role, status
             FROM users
             WHERE email = ?
             LIMIT 1`,
            [email]
        );

        // Generic message — prevents user enumeration
        if (rows.length === 0) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password.",
            });
        }

        const user = rows[0];

        // Block inactive users
        if (user.status === "Inactive") {
            return res.status(403).json({
                success: false,
                message: "Your account has been deactivated. Please contact an administrator.",
            });
        }

        // Password verification (bcrypt mandatory)
        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password.",
            });
        }

        // Sign JWT — id, email, role match actual column names
        const token = jwt.sign(
            {
                id: user.id,
                email: user.email,
                role: user.role,
            },
            process.env.JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN || "8h" }
        );

        return res.status(200).json({
            success: true,
            message: "Login successful.",
            token,
            user: {
                id: user.id,
                name: user.full_name,
                email: user.email,
                role: user.role,
            },
        });
    } catch (err) {
        console.error("Login error:", err.message);
        return res.status(500).json({
            success: false,
            message: "Server error. Please try again later.",
        });
    }
};

// GET /api/me  (protected — requires valid JWT via authMiddleware)
const getMe = (req, res) => {
    // req.user is populated by authMiddleware.verifyToken
    return res.status(200).json({
        success: true,
        user: req.user,
    });
};

module.exports = { login, getMe };