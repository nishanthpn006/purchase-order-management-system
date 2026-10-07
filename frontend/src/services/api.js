import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:5000/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// ── Request interceptor — attach JWT ─────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("poms_token");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor — handle 401 globally ───────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // If unauthorized (invalid/expired JWT) and not on login page
    if (error.response?.status === 401 && window.location.pathname !== "/") {
      localStorage.removeItem("poms_token");
      localStorage.removeItem("poms_user");
      window.location.href = "/";
    }
    return Promise.reject(error);
  }
);

// ── Auth service functions ───────────────────────────────────
export const loginApi                = (credentials) => api.post("/login", credentials);
export const getMe                   = () => api.get("/me");

// ── Dashboard service functions ──────────────────────────────
export const getDashboardStats       = () => api.get("/dashboard/stats");

// ── Vendor service functions ─────────────────────────────────
export const getVendors              = () => api.get("/vendors");
export const getVendorById           = (id) => api.get(`/vendors/${id}`);
export const createVendor            = (data) => api.post("/vendors", data);

// ── Product service functions ────────────────────────────────
export const getProducts             = () => api.get("/products");
export const getProductById          = (id) => api.get(`/products/${id}`);
export const createProduct           = (data) => api.post("/products", data);
export const updateProduct           = (id, data) => api.put(`/products/${id}`, data);

// ── Purchase Order service functions ─────────────────────────
export const getPurchaseOrders       = () => api.get("/purchase-orders");
export const getPurchaseOrderById    = (id) => api.get(`/purchase-orders/${id}`);
export const createPurchaseOrder     = (data) => api.post("/purchase-orders", data);
export const updatePurchaseOrderStatus = (id, status) =>
  api.patch(`/purchase-orders/${id}/status`, { status });

// ── Inventory service functions ──────────────────────────────
export const getInventory            = () => api.get("/inventory");
export const getInventoryById        = (id) => api.get(`/inventory/${id}`);

// ── Goods Receipts service functions ─────────────────────────
export const getGoodsReceipts        = () => api.get("/goods-receipts");
export const getGoodsReceiptById     = (id) => api.get(`/goods-receipts/${id}`);

export default api;