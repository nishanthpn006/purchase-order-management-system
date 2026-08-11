import { useState, useEffect } from "react";
import { Search, RefreshCw } from "lucide-react";
import { getGoodsReceipts } from "../services/api";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

function fmt(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function GoodsReceiptsPage() {
  const [receipts, setReceipts] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");
  const [search, setSearch]     = useState("");

  const load = async () => {
    setLoading(true); setError("");
    try {
      const res = await getGoodsReceipts();
      setReceipts(res.data.data);
    } catch {
      setError("Unable to load goods receipts.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const filtered = receipts.filter(
    (r) =>
      (r.po_number ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.vendor_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.received_by_name ?? "").toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Goods Receipts</div>
            <div className="page-subtitle">
              {!loading && `${receipts.length} receipt${receipts.length !== 1 ? "s" : ""} recorded`}
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <div className="search-box">
              <Search className="search-box-icon" size={16} />
              <input
                type="search"
                placeholder="Search receipts…"
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
          <LoadingState message="Loading goods receipts…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No goods receipts found"
            description={search ? `No receipts match "${search}".` : "Goods receipts will appear here once deliveries are recorded."}
          />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>PO Number</th>
                  <th>Vendor</th>
                  <th>Received Date</th>
                  <th>Received By</th>
                  <th>Remarks</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((r, i) => (
                  <tr key={r.id}>
                    <td className="table-cell-muted">{i + 1}</td>
                    <td className="table-cell-mono table-cell-bold">{r.po_number ?? "—"}</td>
                    <td>{r.vendor_name ?? "—"}</td>
                    <td className="table-cell-muted">{fmt(r.received_date)}</td>
                    <td>{r.received_by_name ?? "—"}</td>
                    <td className="table-cell-muted">{r.remarks ?? "—"}</td>
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

export default GoodsReceiptsPage;
