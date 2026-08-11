import { useState } from "react";
import { AuthContext } from "./useAuth";

const TOKEN_KEY = "poms_token";
const USER_KEY = "poms_user";

// Lazy initializers — run synchronously before the first render.
// This avoids calling setState inside useEffect, which causes cascading renders.

function getInitialToken() {
    return localStorage.getItem(TOKEN_KEY) || null;
}

function getInitialUser() {
    try {
        const stored = localStorage.getItem(USER_KEY);
        return stored ? JSON.parse(stored) : null;
    } catch {
        // Corrupted storage — clear it
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
        localStorage.setItem(TOKEN_KEY, newToken);
        localStorage.setItem(USER_KEY, JSON.stringify(newUser));
        setToken(newToken);
        setUser(newUser);
    };

    /** Call on logout or 401 response */
    const logout = () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider
            value={{ user, token, isAuthenticated: !!token, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
}
