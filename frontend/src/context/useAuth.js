import { useContext, createContext } from "react";

export const AuthContext = createContext(null);

/**
 * useAuth — custom hook to access authentication state and actions.
 * Must be used inside a component wrapped by <AuthProvider>.
 */
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used inside an <AuthProvider>");
    }
    return context;
}
