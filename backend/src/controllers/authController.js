const login = (req, res) => {
    const { email, password } = req.body;

    if (email === "admin@poms.com" && password === "admin123") {
        return res.status(200).json({
            success: true,
            message: "Login Successful",
            user: {
                id: 1,
                name: "Administrator",
                email: "admin@poms.com",
                role: "Admin"
            }
        });
    }

    return res.status(401).json({
        success: false,
        message: "Invalid Email or Password"
    });
};

module.exports = {
    login
};