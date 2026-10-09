import { useState, useEffect } from "react";
import { Search, RefreshCw, Plus, CheckCircle, X, PackageCheck } from "lucide-react";
import { getGoodsReceipts, getPurchaseOrders, getVendors } from "../services/api";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import ReceiveItemsModal from "../components/ReceiveItemsModal";
import "../styles/poms.css";

function fmt(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function GoodsReceiptsPage() {
  const [receipts, setReceipts]             = useState([]);
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [loading, setLoading]               = useState(true);
  const [error, setError]                   = useState("");
  const [successMsg, setSuccessMsg]         = useState("");
  const [search, setSearch]                 = useState("");
  const [refreshKey, setRefreshKey]         = useState(0);
  const [selectedRows, setSelectedRows]     = useState(new Set());

  // PO Selection & Receiving Modal state
  const [isSelectPoOpen, setIsSelectPoOpen] = useState(false);
  const [selectedPoId, setSelectedPoId]     = useState("");
  const [selectError, setSelectError]       = useState("");
  const [receivePoId, setReceivePoId]       = useState(null);

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
            status: o.status || "Pending",
          };
        });

        const normalizedOrders = rawOrders.map((o) => ({
          id: o.id,
          poNumber: o.poNumber || o.po_number || `PO-${o.id}`,
          vendorId: o.vendorId,
          vendorName: o.vendor_name || vendorsMap[o.vendorId] || (o.vendorId ? `Vendor #${o.vendorId}` : "—"),
          status: o.status || "Pending",
        }));

        const normalized = rawReceipts.map((r) => {
          const poInfo = ordersMap[r.purchaseOrderId] || {};
          return {
            id: r.id,
            gr_number: r.grNumber || r.gr_number || (r.id ? `GR-${String(r.id).padStart(4, "0")}` : "—"),
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
          setPurchaseOrders(normalizedOrders);
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

  const approvedOrders = purchaseOrders.filter((po) => po.status === "Approved");

  const filtered = receipts.filter(
    (r) =>
      (r.gr_number ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.po_number ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.vendor_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.received_by_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (r.remarks ?? "").toLowerCase().includes(search.toLowerCase())
  );

  const handleProceedToReceive = () => {
    if (!selectedPoId) {
      setSelectError("Please select an approved purchase order.");
      return;
    }
    const poId = Number(selectedPoId);
    const po = purchaseOrders.find((p) => p.id === poId);
    if (!po || po.status !== "Approved") {
      setSelectError("Selected purchase order is not approved for receiving.");
      return;
    }
    setIsSelectPoOpen(false);
    setReceivePoId(poId);
  };

  const handleCloseReceive = () => {
    setReceivePoId(null);
    setSelectedPoId("");
    setSelectError("");
  };

  const handleReceiveSuccess = (receiptData) => {
    setReceivePoId(null);
    setSelectedPoId("");
    setSelectError("");
    setIsSelectPoOpen(false);
    const grNum = receiptData?.grNumber || receiptData?.gr_number;
    setSuccessMsg(
      grNum
        ? `Goods receipt ${grNum} created successfully.`
        : "Goods receipt created successfully."
    );
    handleRefresh();
  };

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
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setSelectedPoId("");
                setSelectError("");
                setIsSelectPoOpen(true);
              }}
              id="record-goods-receipt-btn"
            >
              <Plus size={14} />
              <span>Record Goods Receipt</span>
            </button>
          </div>
        </div>
      </div>

      {/* Success alert message */}
      {successMsg && (
        <div
          style={{
            background: "var(--emerald-50)",
            border: "1px solid var(--emerald-200)",
            borderRadius: "var(--radius)",
            padding: "9px 14px",
            color: "var(--emerald-700)",
            fontSize: "0.82rem",
            marginBottom: 14,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
          role="status"
        >
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <CheckCircle size={15} />
            <span>{successMsg}</span>
          </div>
          <button
            onClick={() => setSuccessMsg("")}
            style={{ background: "none", border: "none", color: "inherit", cursor: "pointer", display: "flex", alignItems: "center" }}
            aria-label="Dismiss alert"
          >
            <X size={14} />
          </button>
        </div>
      )}

      <div className="card">
        {/* Dedicated Control Toolbar (Reference C) */}
        <div className="table-toolbar">
          <div className="table-toolbar-left">
            <div className="search-box">
              <Search className="search-box-icon" size={14} />
              <input
                type="search"
                placeholder="Search GR, PO, vendor, staff, remarks…"
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
                    <th>GR Number</th>
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
                            aria-label={`Select receipt ${r.gr_number || r.po_number}`}
                          />
                        </td>
                        <td className="table-cell-muted">{i + 1}</td>
                        <td className="table-cell-mono table-cell-bold">{r.gr_number || "—"}</td>
                        <td className="table-cell-mono">{r.po_number ?? "—"}</td>
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

      {/* ── SELECT PURCHASE ORDER MODAL ────────────────────────── */}
      {isSelectPoOpen && (
        <div className="modal-overlay" onClick={() => setIsSelectPoOpen(false)}>
          <div
            className="modal-dialog"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="select-po-title"
            style={{ maxWidth: 500 }}
          >
            <div className="modal-header">
              <div id="select-po-title" className="modal-title">Record Goods Receipt</div>
              <button
                type="button"
                className="btn btn-ghost btn-sm btn-icon"
                onClick={() => setIsSelectPoOpen(false)}
                aria-label="Close dialog"
              >
                <X size={15} />
              </button>
            </div>

            <div className="modal-body">
              <p style={{ margin: "0 0 14px", fontSize: "0.82rem", color: "var(--text-secondary)", lineHeight: 1.5 }}>
                Select an approved purchase order to record received goods into inventory.
              </p>

              {selectError && (
                <div
                  style={{
                    background: "var(--danger-bg)",
                    border: "1px solid var(--danger-border)",
                    borderRadius: "var(--radius)",
                    padding: "8px 12px",
                    color: "var(--danger)",
                    fontSize: "0.8rem",
                    marginBottom: 12,
                  }}
                  role="alert"
                >
                  {selectError}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="select-po-dropdown">Approved Purchase Order *</label>
                {approvedOrders.length === 0 ? (
                  <div
                    style={{
                      padding: "10px 12px",
                      background: "var(--surface-subtle)",
                      border: "1px solid var(--border)",
                      borderRadius: "var(--radius)",
                      fontSize: "0.8rem",
                      color: "var(--text-muted)",
                    }}
                  >
                    No approved purchase orders are currently available for receiving.
                  </div>
                ) : (
                  <select
                    id="select-po-dropdown"
                    value={selectedPoId}
                    onChange={(e) => {
                      setSelectedPoId(e.target.value);
                      setSelectError("");
                    }}
                  >
                    <option value="">Select an approved purchase order…</option>
                    {approvedOrders.map((po) => (
                      <option key={po.id} value={po.id}>
                        {po.poNumber} — {po.vendorName}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => setIsSelectPoOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={!selectedPoId}
                onClick={handleProceedToReceive}
                id="proceed-receive-po-btn"
              >
                <PackageCheck size={13} />
                <span>Receive Items</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── RECEIVE ITEMS MODAL ────────────────────────── */}
      {receivePoId && (
        <ReceiveItemsModal
          purchaseOrderId={receivePoId}
          onClose={handleCloseReceive}
          onSuccess={handleReceiveSuccess}
        />
      )}
    </>
  );
}

export default GoodsReceiptsPage;
