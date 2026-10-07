import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { loginApi, getMe } from "../services/api";
import "../styles/poms.css";

function Login() {
  const navigate = useNavigate();
  const { isAuthenticated, login } = useAuth();

  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [error, setError]       = useState("");
  const [loading, setLoading]   = useState(false);

  // Already authenticated → go straight to dashboard
  useEffect(() => {
    if (isAuthenticated) navigate("/dashboard", { replace: true });
  }, [isAuthenticated, navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await loginApi({ email: email.trim(), password });
      const token = response.data?.token;
      if (!token) {
        throw new Error("Authentication failed: No token received from server.");
      }

      // Temporarily store token so getMe() request interceptor uses it
      localStorage.setItem("poms_token", token);

      let userData = null;
      try {
        const meRes = await getMe();
        userData = meRes.data;
      } catch {
        userData = { email: email.trim(), role: "User" };
      }

      const normalizedUser = {
        id: userData.id,
        name: userData.fullName || userData.name || email.trim().split("@")[0],
        fullName: userData.fullName || userData.name,
        email: userData.email || email.trim(),
        role: userData.role || "User",
        status: userData.status || "Active",
      };

      login(token, normalizedUser);
      navigate("/dashboard", { replace: true });
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 401 ? "Invalid email or password." : null) ||
        (err.response?.status === 403 ? "Access forbidden. Please check your credentials." : null) ||
        err.message ||
        "Unable to connect to server. Please try again.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleLogin} noValidate>
        <div className="login-brand-header">
          <div className="login-brand-badge" aria-hidden="true">
            P
          </div>
          <h1>POMS</h1>
          <p className="subtitle">Enterprise Procurement Management System</p>
        </div>

        {error && (
          <div
            style={{
              background: "var(--danger-bg)",
              border: "1px solid var(--danger-border)",
              borderRadius: "var(--radius)",
              padding: "10px 14px",
              color: "var(--danger)",
              fontSize: "0.82rem",
              marginBottom: 16,
              textAlign: "center",
            }}
            role="alert"
          >
            {error}
          </div>
        )}

        <div className="input-group">
          <label htmlFor="login-email">Email Address</label>
          <input
            id="login-email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
          />
        </div>

        <div className="input-group">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? "Signing in…" : "Sign In"}
        </button>

        <p style={{ textAlign: "center", marginTop: 20, fontSize: "0.75rem", color: "var(--text-muted)" }}>
          Purchase Order Management System &copy; 2026
        </p>
      </form>
    </div>
  );
}

export default Login;