const jwt = require("jsonwebtoken");

/**
 * Middleware: verifyToken
 * Expects: Authorization: Bearer <token>
 * On success: attaches decoded payload to req.user and calls next()
 * On failure: returns 401/403 JSON error
 */
const verifyToken = (req, res, next) => {
    const authHeader = req.headers["authorization"];

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({
            success: false,
            message: "Access denied. No token provided.",
        });
    }

    const token = authHeader.split(" ")[1];

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.user = decoded; // { id, email, role, iat, exp }
        next();
    } catch (err) {
        if (err.name === "TokenExpiredError") {
            return res.status(401).json({
                success: false,
                message: "Session expired. Please log in again.",
            });
        }
        return res.status(403).json({
            success: false,
            message: "Invalid token.",
        });
    }
};

module.exports = { verifyToken };
