import { useState, useEffect } from "react";
import {
  Users, Package, ShoppingCart, Boxes,
  AlertTriangle, Clock, ShoppingBag,
} from "lucide-react";
import { useAuth } from "../context/useAuth";
import { getDashboardStats, getPurchaseOrders, getInventory } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

/* ── Helpers ─────────────────────────────────────────────── */
function fmt(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function fmtCurrency(amount) {
  if (amount == null) return "—";
  return "₹" + Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2 });
}

function getStockStatus(qty, reorder) {
  if (qty === 0) return "Reorder Required";
  if (qty <= reorder) return "Low Stock";
  return "In Stock";
}

/* ── KPI Card ────────────────────────────────────────────── */
function KPICard({ icon: Icon, iconClass, value, label, desc, loading }) {
  return (
    <div className="kpi-card">
      <div className={`kpi-icon ${iconClass}`}>
        <Icon size={22} />
      </div>
      <div className="kpi-body">
        <div className="kpi-value">{loading ? "—" : value}</div>
        <div className="kpi-label">{label}</div>
        <div className="kpi-desc">{desc}</div>
      </div>
    </div>
  );
}

/* ── Dashboard Page ──────────────────────────────────────── */
function Dashboard() {
  const { user } = useAuth();

  const [stats, setStats]       = useState(null);
  const [orders, setOrders]     = useState([]);
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const [statsRes, ordersRes, invRes] = await Promise.all([
          getDashboardStats(),
          getPurchaseOrders(),
          getInventory(),
        ]);
        setStats(statsRes.data.data);
        setOrders(ordersRes.data.data.slice(0, 5));      // latest 5
        setInventory(invRes.data.data.slice(0, 5));       // top 5
      } catch {
        setError("Unable to load dashboard data. Please refresh the page.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <>
      {/* Welcome banner */}
      <div className="welcome-banner">
        <div className="welcome-text">
          <h2>Welcome back, {user?.name ?? "Administrator"} 👋</h2>
          <p>Here&apos;s an overview of your procurement operations.</p>
        </div>
        <div className="welcome-icon">
          <ShoppingBag />
        </div>
      </div>

      {/* Error */}
      {error && (
        <div
          style={{
            background: "var(--danger-bg)", border: "1px solid var(--danger-border)",
            borderRadius: "var(--radius)", padding: "14px 18px", marginBottom: 20,
            color: "var(--danger)", fontSize: "0.85rem", display: "flex", gap: 8,
          }}
        >
          <AlertTriangle size={16} style={{ flexShrink: 0, marginTop: 1 }} />
          {error}
        </div>
      )}

      {/* KPI grid */}
      <div className="kpi-grid">
        <KPICard
          icon={Users}
          iconClass="kpi-icon-blue"
          value={stats?.total_vendors}
          label="Total Vendors"
          desc="Active supplier accounts"
          loading={loading}
        />
        <KPICard
          icon={Package}
          iconClass="kpi-icon-green"
          value={stats?.total_products}
          label="Total Products"
          desc="Items in product catalog"
          loading={loading}
        />
        <KPICard
          icon={ShoppingCart}
          iconClass="kpi-icon-amber"
          value={stats?.total_orders}
          label="Purchase Orders"
          desc={`${stats?.pending_orders ?? "—"} pending approval`}
          loading={loading}
        />
        <KPICard
          icon={Boxes}
          iconClass="kpi-icon-cyan"
          value={stats?.total_inventory}
          label="Inventory Items"
          desc={`${stats?.low_stock ?? "—"} low stock alerts`}
          loading={loading}
        />
      </div>

      {/* Two-column grid */}
      <div className="dashboard-grid">

        {/* Recent Purchase Orders */}
        <div className="card">
          <div className="card-header" style={{ paddingBottom: 14 }}>
            <div>
              <div className="card-title">Recent Purchase Orders</div>
              <div className="card-subtitle">Latest procurement activity</div>
            </div>
            <Clock size={17} color="var(--text-muted)" />
          </div>

          {loading ? (
            <LoadingState message="Loading orders…" />
          ) : orders.length === 0 ? (
            <EmptyState description="No purchase orders have been created yet." />
          ) : (
            <div className="table-container">
              <table className="table">
                <thead>
                  <tr>
                    <th>PO Number</th>
                    <th>Vendor</th>
                    <th>Amount</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((po) => (
                    <tr key={po.id}>
                      <td className="table-cell-mono table-cell-bold">{po.po_number}</td>
                      <td style={{ maxWidth: 140, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {po.vendor_name}
                      </td>
                      <td style={{ fontWeight: 600 }}>{fmtCurrency(po.total_amount)}</td>
                      <td><StatusBadge status={po.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Inventory Summary */}
        <div className="card">
          <div className="card-header" style={{ paddingBottom: 14 }}>
            <div>
              <div className="card-title">Inventory Summary</div>
              <div className="card-subtitle">Current stock levels</div>
            </div>
            <Boxes size={17} color="var(--text-muted)" />
          </div>

          {loading ? (
            <LoadingState message="Loading inventory…" />
          ) : inventory.length === 0 ? (
            <EmptyState description="No inventory records found." />
          ) : (
            <div className="table-container">
              <table className="table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th style={{ textAlign: "center" }}>Qty</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {inventory.map((item) => {
                    const stockStatus = getStockStatus(item.quantity_in_stock, item.reorder_level);
                    return (
                      <tr key={item.id}>
                        <td style={{ maxWidth: 160, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                          {item.product_name}
                        </td>
                        <td style={{ textAlign: "center", fontWeight: 600 }}>{item.quantity_in_stock}</td>
                        <td><StatusBadge status={stockStatus} /></td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  );
}

export default Dashboard;