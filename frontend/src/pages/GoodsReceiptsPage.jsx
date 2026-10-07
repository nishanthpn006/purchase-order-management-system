import { useState, useEffect } from "react";
import { Search, RefreshCw, Plus } from "lucide-react";
import { getGoodsReceipts, getPurchaseOrders, getVendors } from "../services/api";
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
  const [receipts, setReceipts]   = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState("");
  const [search, setSearch]       = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [selectedRows, setSelectedRows] = useState(new Set());

  const handleRefresh = () => {
    setLoading(true);
    setError("");
    setRefreshKey((k) => k + 1);
  };

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        const [grRes, poRes, vendRes] = await Promise.all([
          getGoodsReceipts(),
          getPurchaseOrders().catch(() => ({ data: [] })),
          getVendors().catch(() => ({ data: [] })),
        ]);

        const rawReceipts = Array.isArray(grRes.data)
          ? grRes.data
          : (grRes.data?.data || []);
        const rawOrders = Array.isArray(poRes.data)
          ? poRes.data
          : (poRes.data?.data || []);
        const rawVends = Array.isArray(vendRes.data)
          ? vendRes.data
          : (vendRes.data?.data || []);

        const vendorsMap = {};
        rawVends.forEach((v) => {
          vendorsMap[v.id] = v.vendorName || v.vendor_name;
        });

        const ordersMap = {};
        rawOrders.forEach((o) => {
          ordersMap[o.id] = {
            poNumber: o.poNumber || o.po_number,
            vendorName: o.vendor_name || vendorsMap[o.vendorId],
          };
        });

        const normalized = rawReceipts.map((r) => {
          const poInfo = ordersMap[r.purchaseOrderId] || {};
          return {
            id: r.id,
            purchase_order_id: r.purchaseOrderId,
            po_number: r.poNumber || r.po_number || poInfo.poNumber || `PO #${r.purchaseOrderId || "—"}`,
            vendor_name: r.vendor_name || poInfo.vendorName || "—",
            received_date: r.receivedDate || r.received_date,
            received_by: r.receivedBy,
            received_by_name: r.received_by_name || (r.receivedBy ? `User #${r.receivedBy}` : "Warehouse Staff"),
            remarks: r.remarks || "—",
          };
        });

        if (!ignore) {
          setReceipts(normalized);
        }
      } catch {
        if (!ignore) {
          setError("Unable to load goods receipts information.");
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    load();
    return () => {
      ignore = true;
    };
  }, [refreshKey]);

  const filtered = receipts.filter(
    (r) =>
      (r.po_number ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.vendor_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.received_by_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.remarks ?? "").toLowerCase().includes(search.toLowerCase())
  );

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedRows(new Set(filtered.map((r) => r.id)));
    } else {
      setSelectedRows(new Set());
    }
  };

  const handleSelectRow = (id) => {
    const next = new Set(selectedRows);
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    setSelectedRows(next);
  };

  return (
    <>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Goods Receipts</div>
            <div className="page-subtitle">
              Delivery verification records, receiving personnel logs, and order intake notes.
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} title="Refresh receipts">
              <RefreshCw size={13} />
              <span>Refresh</span>
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        {/* Dedicated Control Toolbar (Reference C) */}
        <div className="table-toolbar">
          <div className="table-toolbar-left">
            <div className="search-box">
              <Search className="search-box-icon" size={14} />
              <input
                type="search"
                placeholder="Search PO, vendor, staff, remarks…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                aria-label="Search goods receipts"
              />
            </div>

            <div className="table-filter-group">
              <button
                type="button"
                className="table-filter-btn"
                title="Add custom filter"
              >
                <Plus size={12} />
                <span>Add filter</span>
              </button>

              {search && (
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  style={{ fontSize: "0.74rem", padding: "3px 8px" }}
                  onClick={() => setSearch("")}
                >
                  Clear search
                </button>
              )}
            </div>
          </div>

          <div className="table-toolbar-right">
            <span style={{ fontSize: "0.74rem", color: "var(--text-muted)" }}>
              Showing {filtered.length} of {receipts.length} receipts
            </span>
            <button
              className="btn btn-ghost btn-sm btn-icon"
              onClick={handleRefresh}
              title="Refresh receipts"
              aria-label="Refresh receipts"
            >
              <RefreshCw size={13} />
            </button>
          </div>
        </div>

        {error ? (
          <div className="empty-state">
            <p style={{ color: "var(--danger)", fontSize: "0.85rem" }}>{error}</p>
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} style={{ marginTop: 8 }}>
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
          <>
            <div className="table-container">
              <table className="table">
                <thead>
                  <tr>
                    <th style={{ width: 36, textAlign: "center" }}>
                      <input
                        type="checkbox"
                        className="table-checkbox"
                        checked={filtered.length > 0 && selectedRows.size === filtered.length}
                        onChange={handleSelectAll}
                        aria-label="Select all receipts"
                      />
                    </th>
                    <th style={{ width: 44 }}>#</th>
                    <th>PO Number</th>
                    <th>Vendor</th>
                    <th>Received Date</th>
                    <th>Received By</th>
                    <th>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((r, i) => {
                    const isSelected = selectedRows.has(r.id);
                    return (
                      <tr key={r.id} className={isSelected ? "row-selected" : ""}>
                        <td style={{ textAlign: "center" }}>
                          <input
                            type="checkbox"
                            className="table-checkbox"
                            checked={isSelected}
                            onChange={() => handleSelectRow(r.id)}
                            aria-label={`Select receipt ${r.po_number}`}
                          />
                        </td>
                        <td className="table-cell-muted">{i + 1}</td>
                        <td className="table-cell-mono table-cell-bold">{r.po_number ?? "—"}</td>
                        <td>{r.vendor_name ?? "—"}</td>
                        <td className="table-cell-muted">{fmt(r.received_date)}</td>
                        <td>{r.received_by_name ?? "—"}</td>
                        <td className="table-cell-muted">{r.remarks ?? "—"}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Reference C Table Pagination Footer */}
            <div className="table-pagination">
              <div className="table-pagination-info">
                Showing {filtered.length > 0 ? 1 : 0} to {filtered.length} of {filtered.length} entries
                {selectedRows.size > 0 && ` (${selectedRows.size} selected)`}
              </div>
              <div className="table-pagination-nav">
                <button className="pagination-btn" disabled>
                  Previous
                </button>
                <button className="pagination-btn active">1</button>
                <button className="pagination-btn" disabled>
                  Next
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </>
  );
}

export default GoodsReceiptsPage;
