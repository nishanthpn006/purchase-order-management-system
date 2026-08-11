import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import VendorsPage from "./pages/VendorsPage";
import ProductsPage from "./pages/ProductsPage";
import PurchaseOrdersPage from "./pages/PurchaseOrdersPage";
import InventoryPage from "./pages/InventoryPage";
import GoodsReceiptsPage from "./pages/GoodsReceiptsPage";

function App() {
  return (
    <AuthProvider>
      <Routes>
          {/* Public */}
          <Route path="/" element={<Login />} />

          {/* Protected — all wrapped inside the Layout shell */}
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard"       element={<Dashboard />} />
            <Route path="/vendors"         element={<VendorsPage />} />
            <Route path="/products"        element={<ProductsPage />} />
            <Route path="/purchase-orders" element={<PurchaseOrdersPage />} />
            <Route path="/inventory"       element={<InventoryPage />} />
            <Route path="/goods-receipts"  element={<GoodsReceiptsPage />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;