import { useState, useEffect } from "react";
import { Search, RefreshCw } from "lucide-react";
import { getInventory } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

function fmtDate(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function getStockStatus(qty, reorder) {
  if (qty === 0) return "Reorder Required";
  if (qty <= reorder) return "Low Stock";
  return "In Stock";
}

function InventoryPage() {
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState("");
  const [search, setSearch]       = useState("");

  const load = async () => {
    setLoading(true); setError("");
    try {
      const res = await getInventory();
      setInventory(res.data.data);
    } catch {
      setError("Unable to load inventory information.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const filtered = inventory.filter(
    (item) =>
      item.product_name.toLowerCase().includes(search.toLowerCase()) ||
      (item.category ?? "").toLowerCase().includes(search.toLowerCase())
  );

  const lowCount = inventory.filter((i) => i.quantity_in_stock <= i.reorder_level).length;

  return (
    <>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Inventory</div>
            <div className="page-subtitle">
              {!loading && `${inventory.length} items tracked${lowCount > 0 ? ` · ${lowCount} low stock alert${lowCount !== 1 ? "s" : ""}` : ""}`}
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <div className="search-box">
              <Search className="search-box-icon" size={16} />
              <input
                type="search"
                placeholder="Search products…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button className="btn btn-ghost btn-sm" onClick={load} title="Refresh">
              <RefreshCw size={14} />
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
          <LoadingState message="Loading inventory…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No inventory records found"
            description={search ? `No items match "${search}".` : "Inventory records will appear here."}
          />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Product</th>
                  <th>Category</th>
                  <th>Vendor</th>
                  <th style={{ textAlign: "center" }}>Qty in Stock</th>
                  <th style={{ textAlign: "center" }}>Reorder Level</th>
                  <th>Last Updated</th>
                  <th>Stock Status</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item, i) => {
                  const status = getStockStatus(item.quantity_in_stock, item.reorder_level);
                  return (
                    <tr key={item.id}>
                      <td className="table-cell-muted">{i + 1}</td>
                      <td className="table-cell-bold">{item.product_name}</td>
                      <td className="table-cell-muted">{item.category ?? "—"}</td>
                      <td className="table-cell-muted">{item.vendor_name ?? "—"}</td>
                      <td style={{ textAlign: "center", fontWeight: 600 }}>{item.quantity_in_stock}</td>
                      <td style={{ textAlign: "center" }} className="table-cell-muted">{item.reorder_level}</td>
                      <td className="table-cell-muted">{fmtDate(item.last_updated)}</td>
                      <td><StatusBadge status={status} /></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}

export default InventoryPage;
