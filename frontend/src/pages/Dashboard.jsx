import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Users, Package, ShoppingCart, Boxes,
  AlertTriangle, AlertCircle,
} from "lucide-react";
import {
  getDashboardStats,
  getPurchaseOrders,
  getInventory,
  getProducts,
  getVendors,
} from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

/* ── Helpers ─────────────────────────────────────────────── */
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
        <Icon size={17} />
      </div>
      <div className="kpi-body">
        <div className="kpi-label">{label}</div>
        <div className="kpi-value">{loading ? "—" : value}</div>
        <div className="kpi-desc">{desc}</div>
      </div>
    </div>
  );
}

/* ── Dashboard Page ──────────────────────────────────────── */
function Dashboard() {
  const [stats, setStats]         = useState(null);
  const [orders, setOrders]       = useState([]);
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const [statsRes, ordersRes, invRes, prodRes, vendRes] = await Promise.all([
          getDashboardStats(),
          getPurchaseOrders(),
          getInventory(),
          getProducts().catch(() => ({ data: [] })),
          getVendors().catch(() => ({ data: [] })),
        ]);

        const rawStats = statsRes.data?.data || statsRes.data || {};
        const rawOrders = Array.isArray(ordersRes.data)
          ? ordersRes.data
          : (ordersRes.data?.data || []);
        const rawInv = Array.isArray(invRes.data)
          ? invRes.data
          : (invRes.data?.data || []);
        const rawProds = Array.isArray(prodRes.data)
          ? prodRes.data
          : (prodRes.data?.data || []);
        const rawVends = Array.isArray(vendRes.data)
          ? vendRes.data
          : (vendRes.data?.data || []);

        const vendorsMap = {};
        rawVends.forEach((v) => {
          vendorsMap[v.id] = v.vendorName || v.vendor_name;
        });

        const productsMap = {};
        rawProds.forEach((p) => {
          productsMap[p.id] = p.productName || p.product_name;
        });

        const normalizedStats = {
          total_vendors: rawStats.totalVendors ?? rawStats.total_vendors ?? rawVends.length,
          total_products: rawStats.totalProducts ?? rawStats.total_products ?? rawProds.length,
          total_orders: rawStats.totalPurchaseOrders ?? rawStats.total_orders ?? rawOrders.length,
          pending_orders: rawStats.pendingOrders ?? rawStats.pending_orders ?? 0,
          total_inventory: rawStats.totalProducts ?? rawStats.total_inventory ?? rawInv.length,
          low_stock: rawStats.lowStockItems ?? rawStats.low_stock ?? 0,
        };

        const normalizedOrders = rawOrders.slice(0, 5).map((po) => ({
          id: po.id,
          po_number: po.poNumber || po.po_number || `PO-${po.id}`,
          vendor_name: po.vendor_name || vendorsMap[po.vendorId] || `Vendor #${po.vendorId || "—"}`,
          total_amount: po.totalAmount ?? po.total_amount,
          status: po.status,
        }));

        const normalizedInv = rawInv.slice(0, 5).map((item) => ({
          id: item.id,
          product_name: item.product_name || productsMap[item.productId] || `Product #${item.productId || "—"}`,
          quantity_in_stock: item.quantityInStock ?? item.quantity_in_stock ?? 0,
          reorder_level: item.reorderLevel ?? item.reorder_level ?? 0,
        }));

        setStats(normalizedStats);
        setOrders(normalizedOrders);
        setInventory(normalizedInv);
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
      {/* Page Header */}
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Procurement Operations</div>
            <div className="page-subtitle">
              Real-time procurement KPIs, approval queues, and warehouse stock monitoring.
            </div>
          </div>
        </div>
      </div>

      {/* Operational Status Banner (Reference A Pattern) */}
      <div className="welcome-banner">
        <div className="welcome-text">
          <h2>
            {(stats?.pending_orders || 0) + (stats?.low_stock || 0) > 0
              ? `${(stats?.pending_orders || 0) + (stats?.low_stock || 0)} Operational items require attention`
              : "Procurement operations running normally"}
          </h2>
          <p>
            {(stats?.pending_orders || 0) + (stats?.low_stock || 0) > 0
              ? `${stats?.pending_orders ?? 0} purchase orders pending approval · ${stats?.low_stock ?? 0} inventory items at or below reorder threshold.`
              : "All purchase orders processed and inventory stocks within safe thresholds."}
          </p>
        </div>
        <div className="welcome-icon">
          <AlertCircle size={16} />
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
          desc="Registered supplier records"
          loading={loading}
        />
        <KPICard
          icon={Package}
          iconClass="kpi-icon-green"
          value={stats?.total_products}
          label="Catalog Products"
          desc="Active catalog item SKUs"
          loading={loading}
        />
        <KPICard
          icon={ShoppingCart}
          iconClass="kpi-icon-amber"
          value={stats?.total_orders}
          label="Purchase Orders"
          desc={`${stats?.pending_orders ?? "—"} orders awaiting review`}
          loading={loading}
        />
        <KPICard
          icon={Boxes}
          iconClass="kpi-icon-cyan"
          value={stats?.total_inventory}
          label="Inventory Stock"
          desc={`${stats?.low_stock ?? "—"} reorder alerts flagged`}
          loading={loading}
        />
      </div>

      {/* Two-column grid */}
      <div className="dashboard-grid">

        {/* Recent Purchase Orders */}
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">Recent Purchase Orders</div>
              <div className="card-subtitle">Latest procurement order activities</div>
            </div>
            <Link to="/purchase-orders" className="btn btn-ghost btn-sm">
              View All
            </Link>
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
                    <th style={{ textAlign: "right" }}>Amount</th>
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
                      <td className="table-num" style={{ fontWeight: 600 }}>{fmtCurrency(po.total_amount)}</td>
                      <td><StatusBadge status={po.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Inventory Attention */}
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">Inventory Attention</div>
              <div className="card-subtitle">Stock levels and reorder thresholds</div>
            </div>
            <Link to="/inventory" className="btn btn-ghost btn-sm">
              View All
            </Link>
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
                    <th style={{ textAlign: "center" }}>In Stock</th>
                    <th style={{ textAlign: "center" }}>Reorder Level</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {inventory.map((item) => {
                    const stockStatus = getStockStatus(item.quantity_in_stock, item.reorder_level);
                    return (
                      <tr key={item.id}>
                        <td style={{ maxWidth: 160, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} className="table-cell-bold">
                          {item.product_name}
                        </td>
                        <td style={{ textAlign: "center", fontWeight: 600 }}>{item.quantity_in_stock}</td>
                        <td style={{ textAlign: "center" }} className="table-cell-muted">{item.reorder_level}</td>
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