import { useState, useEffect } from "react";
import { AuthContext } from "./useAuth";
import { getMe } from "../services/api";

const TOKEN_KEY = "poms_token";
const USER_KEY = "poms_user";

function getInitialToken() {
    return localStorage.getItem(TOKEN_KEY) || null;
}

function getInitialUser() {
    try {
        const stored = localStorage.getItem(USER_KEY);
        if (!stored) return null;
        const u = JSON.parse(stored);
        if (u && !u.name && u.fullName) {
            u.name = u.fullName;
        }
        return u;
    } catch {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        return null;
    }
}

export function AuthProvider({ children }) {
    const [token, setToken] = useState(getInitialToken);
    const [user, setUser] = useState(getInitialUser);

    /** Call after a successful login API response */
    const login = (newToken, newUser) => {
        const normalized = {
            ...newUser,
            name: newUser?.fullName || newUser?.name || "User",
            fullName: newUser?.fullName || newUser?.name || "User",
        };
        localStorage.setItem(TOKEN_KEY, newToken);
        localStorage.setItem(USER_KEY, JSON.stringify(normalized));
        setToken(newToken);
        setUser(normalized);
    };

    /** Call on logout or 401 response */
    const logout = () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setToken(null);
        setUser(null);
    };

    // Sync user profile on mount if token exists
    useEffect(() => {
        if (token) {
            getMe()
                .then((res) => {
                    const u = res.data;
                    const normalized = {
                        id: u.id,
                        name: u.fullName || u.name,
                        fullName: u.fullName || u.name,
                        email: u.email,
                        role: u.role,
                        status: u.status,
                    };
                    localStorage.setItem(USER_KEY, JSON.stringify(normalized));
                    setUser(normalized);
                })
                .catch(() => {
                    // Token expired or invalid
                    logout();
                });
        }
    }, [token]);

    return (
        <AuthContext.Provider
            value={{ user, token, isAuthenticated: !!token, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
}
