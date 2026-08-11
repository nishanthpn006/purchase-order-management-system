import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:5000/api",
});

// ── Request interceptor — attach JWT ─────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("poms_token");
    if (token) config.headers["Authorization"] = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor — handle 401 globally ───────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("poms_token");
      localStorage.removeItem("poms_user");
      window.location.href = "/";
    }
    return Promise.reject(error);
  }
);

// ── API service functions ────────────────────────────────────
export const getDashboardStats   = () => api.get("/dashboard/stats");
export const getVendors          = () => api.get("/vendors");
export const getProducts         = () => api.get("/products");
export const getPurchaseOrders   = () => api.get("/purchase-orders");
export const getInventory        = () => api.get("/inventory");
export const getGoodsReceipts    = () => api.get("/goods-receipts");

export default api;