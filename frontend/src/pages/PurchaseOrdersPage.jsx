import { useState, useEffect } from "react";
import { Search, FilePlus, RefreshCw } from "lucide-react";
import { getPurchaseOrders } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

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

function PurchaseOrdersPage() {
  const [orders, setOrders]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState("");
  const [search, setSearch]   = useState("");

  const load = async () => {
    setLoading(true); setError("");
    try {
      const res = await getPurchaseOrders();
      setOrders(res.data.data);
    } catch {
      setError("Unable to load purchase orders.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const filtered = orders.filter(
    (po) =>
      po.po_number.toLowerCase().includes(search.toLowerCase()) ||
      (po.vendor_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (po.status ?? "").toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Purchase Orders</div>
            <div className="page-subtitle">
              {!loading && `${orders.length} order${orders.length !== 1 ? "s" : ""} total`}
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <div className="search-box">
              <Search className="search-box-icon" size={16} />
              <input
                type="search"
                placeholder="Search orders…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button className="btn btn-ghost btn-sm" onClick={load} title="Refresh">
              <RefreshCw size={14} />
            </button>
            <button className="btn btn-primary btn-sm">
              <FilePlus size={14} />
              New PO
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        {error ? (
          <div className="empty-state">
            <p style={{ color: "var(--danger)", fontSize: "0.88rem" }}>{error}</p>
            <button className="btn btn-ghost btn-sm" onClick={load} style={{ marginTop: 8 }}>
              <RefreshCw size={13} /> Retry
            </button>
          </div>
        ) : loading ? (
          <LoadingState message="Loading purchase orders…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No purchase orders found"
            description={search ? `No orders match "${search}".` : "No purchase orders have been created yet."}
          />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>PO Number</th>
                  <th>Vendor</th>
                  <th>Order Date</th>
                  <th>Expected Delivery</th>
                  <th>Total Amount</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((po) => (
                  <tr key={po.id}>
                    <td className="table-cell-mono table-cell-bold">{po.po_number}</td>
                    <td>{po.vendor_name ?? "—"}</td>
                    <td className="table-cell-muted">{fmt(po.order_date)}</td>
                    <td className="table-cell-muted">{fmt(po.expected_delivery)}</td>
                    <td style={{ fontWeight: 600 }}>{fmtCurrency(po.total_amount)}</td>
                    <td><StatusBadge status={po.status} /></td>
                    <td>
                      <button className="btn btn-ghost btn-sm">View</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}

export default PurchaseOrdersPage;
