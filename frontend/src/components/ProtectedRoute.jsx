import { Navigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

/**
 * ProtectedRoute — wraps any route that requires authentication.
 * - While auth state is loading (localStorage rehydration), renders nothing.
 * - If not authenticated, redirects to "/" (Login page).
 * - If authenticated, renders the children as-is.
 */
function ProtectedRoute({ children }) {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return children;
}

export default ProtectedRoute;
