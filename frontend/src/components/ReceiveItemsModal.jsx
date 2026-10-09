import { useState, useEffect } from "react";
import { X, RefreshCw, AlertTriangle, PackageCheck } from "lucide-react";
import { getPurchaseOrderReceivingDetails, createGoodsReceipt } from "../services/api";
import StatusBadge from "./StatusBadge";
import LoadingState from "./LoadingState";

function fmtDate(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function getTodayLocalDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * Modal dialog for receiving items against an approved purchase order.
 * Calls getPurchaseOrderReceivingDetails to load lines and createGoodsReceipt to submit.
 */
function ReceiveItemsModal({ purchaseOrderId, onClose, onSuccess }) {
  const [details, setDetails] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [fetchError, setFetchError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  // Form fields
  const [receivedDate, setReceivedDate] = useState(getTodayLocalDate);
  const [remarks, setRemarks] = useState("");
  const [receiveQuantities, setReceiveQuantities] = useState({});

  const handleRetry = () => {
    setIsLoading(true);
    setFetchError("");
    setSubmitError("");
    setRefreshKey((k) => k + 1);
  };

  useEffect(() => {
    let ignore = false;
    if (!purchaseOrderId) return;

    async function fetchDetails() {
      try {
        const res = await getPurchaseOrderReceivingDetails(purchaseOrderId);
        if (ignore) return;
        const data = res.data;
        setDetails(data);

        // Initialize all product lines to 0
        const initialQuantities = {};
        if (Array.isArray(data?.items)) {
          data.items.forEach((item) => {
            initialQuantities[item.productId] = 0;
          });
        }
        setReceiveQuantities(initialQuantities);
        setFetchError("");
      } catch (err) {
        if (ignore) return;
        const msg =
          err.response?.data?.error ||
          err.response?.data?.message ||
          err.message ||
          "Failed to load purchase order receiving details. Please try again.";
        setFetchError(msg);
      } finally {
        if (!ignore) {
          setIsLoading(false);
        }
      }
    }

    fetchDetails();

    return () => {
      ignore = true;
    };
  }, [purchaseOrderId, refreshKey]);

  // Close on Escape key
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape" && !isSubmitting && onClose) {
        onClose();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isSubmitting, onClose]);

  const handleQuantityChange = (productId, rawVal) => {
    setSubmitError("");
    setReceiveQuantities((prev) => ({
      ...prev,
      [productId]: rawVal,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isSubmitting) return;

    if (!details) {
      setSubmitError("Purchase order details are not loaded.");
      return;
    }

    if (details.status !== "Approved") {
      setSubmitError("Goods receipt can only be created for purchase orders in 'Approved' status.");
      return;
    }

    if (!receivedDate || !receivedDate.trim()) {
      setSubmitError("Received date is required.");
      return;
    }

    const itemsToSubmit = [];
    for (const item of (details.items || [])) {
      const rawVal = receiveQuantities[item.productId];
      const valStr = String(rawVal ?? "").trim();

      if (!valStr || valStr === "0") {
        continue;
      }

      const num = Number(valStr);
      if (!Number.isInteger(num)) {
        setSubmitError(`Received quantity for "${item.productName}" must be a whole integer.`);
        return;
      }
      if (num < 0) {
        setSubmitError(`Received quantity for "${item.productName}" cannot be negative.`);
        return;
      }
      if (num > (item.remainingQuantity ?? 0)) {
        setSubmitError(
          `Received quantity for "${item.productName}" (${num}) exceeds the remaining quantity (${item.remainingQuantity ?? 0}).`
        );
        return;
      }

      itemsToSubmit.push({
        productId: item.productId,
        receivedQuantity: num,
      });
    }

    if (itemsToSubmit.length === 0) {
      setSubmitError("Please enter a quantity greater than zero for at least one item to receive.");
      return;
    }

    const payload = {
      purchaseOrderId: Number(purchaseOrderId),
      receivedDate: receivedDate.trim(),
      remarks: remarks.trim() ? remarks.trim() : undefined,
      items: itemsToSubmit,
    };

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const res = await createGoodsReceipt(payload);
      if (onSuccess) {
        onSuccess(res.data);
      }
      if (onClose) {
        onClose();
      }
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Failed to submit goods receipt. Please check entered quantities.";
      setSubmitError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const isApproved = details?.status === "Approved";

  return (
    <div
      className="modal-overlay"
      onClick={() => {
        if (!isSubmitting && onClose) onClose();
      }}
    >
      <div
        className="modal-dialog modal-dialog-lg"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="receive-modal-title"
      >
        <div className="modal-header">
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <PackageCheck size={18} style={{ color: "var(--primary)" }} />
            <div id="receive-modal-title" className="modal-title">
              Receive Items {details?.poNumber ? `— ${details.poNumber}` : ""}
            </div>
            {details?.status && <StatusBadge status={details.status} />}
          </div>
          <button
            type="button"
            className="btn btn-ghost btn-sm btn-icon"
            onClick={onClose}
            disabled={isSubmitting}
            aria-label="Close dialog"
          >
            <X size={15} />
          </button>
        </div>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
          <div className="modal-body">
            {isLoading ? (
              <LoadingState message="Loading purchase order receiving details…" />
            ) : fetchError ? (
              <div
                style={{
                  background: "var(--danger-bg)",
                  border: "1px solid var(--danger-border)",
                  borderRadius: "var(--radius)",
                  padding: "14px 16px",
                  color: "var(--danger)",
                  fontSize: "0.84rem",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  gap: 12,
                }}
                role="alert"
              >
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <AlertTriangle size={16} style={{ flexShrink: 0 }} />
                  <span>{fetchError}</span>
                </div>
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={handleRetry}
                  style={{ color: "var(--danger)" }}
                >
                  <RefreshCw size={13} />
                  <span>Retry</span>
                </button>
              </div>
            ) : details ? (
              <>
                {/* Status Warning if not Approved */}
                {!isApproved && (
                  <div
                    style={{
                      background: "var(--warning-bg)",
                      border: "1px solid var(--warning-border)",
                      borderRadius: "var(--radius)",
                      padding: "9px 12px",
                      color: "var(--warning-text)",
                      fontSize: "0.82rem",
                      marginBottom: 14,
                      display: "flex",
                      alignItems: "center",
                      gap: 8,
                    }}
                    role="alert"
                  >
                    <AlertTriangle size={15} style={{ flexShrink: 0 }} />
                    <span>
                      This purchase order is currently in <strong>{details.status}</strong> status. Goods receipts can only be processed for <strong>Approved</strong> purchase orders.
                    </span>
                  </div>
                )}

                {/* Submit error alert */}
                {submitError && (
                  <div
                    style={{
                      background: "var(--danger-bg)",
                      border: "1px solid var(--danger-border)",
                      borderRadius: "var(--radius)",
                      padding: "9px 12px",
                      color: "var(--danger)",
                      fontSize: "0.82rem",
                      marginBottom: 14,
                      display: "flex",
                      alignItems: "center",
                      gap: 8,
                    }}
                    role="alert"
                  >
                    <AlertTriangle size={15} style={{ flexShrink: 0 }} />
                    <span>{submitError}</span>
                  </div>
                )}

                {/* Purchase Order Summary Strip */}
                <div className="summary-strip">
                  <div className="summary-tile">
                    <span className="summary-tile-label">PO Number</span>
                    <span className="summary-tile-value">{details.poNumber || "—"}</span>
                  </div>
                  <div className="summary-tile">
                    <span className="summary-tile-label">Vendor</span>
                    <span className="summary-tile-value" style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {details.vendorName || `Vendor #${details.vendorId || "—"}`}
                    </span>
                  </div>
                  <div className="summary-tile">
                    <span className="summary-tile-label">Status</span>
                    <span className="summary-tile-value">
                      <StatusBadge status={details.status} />
                    </span>
                  </div>
                  <div className="summary-tile">
                    <span className="summary-tile-label">Order Date</span>
                    <span className="summary-tile-value">{fmtDate(details.orderDate)}</span>
                  </div>
                </div>

                {/* Line Items Table */}
                <div style={{ marginBottom: 16 }}>
                  <div className="modal-section-title" style={{ marginBottom: 8 }}>
                    Receiving Line Items
                  </div>
                  <div style={{ overflowX: "auto", border: "1px solid var(--border)", borderRadius: "var(--radius)" }}>
                    <table className="table" style={{ width: "100%", margin: 0 }}>
                      <thead>
                        <tr>
                          <th>Product</th>
                          <th className="table-num">Ordered</th>
                          <th className="table-num">Previously Received</th>
                          <th className="table-num">Remaining</th>
                          <th style={{ width: 140, textAlign: "right" }}>Receive Now</th>
                        </tr>
                      </thead>
                      <tbody>
                        {details.items && details.items.length > 0 ? (
                          details.items.map((item) => {
                            const remaining = item.remainingQuantity ?? 0;
                            const isFullyReceived = remaining <= 0;
                            const currentInputVal = receiveQuantities[item.productId] ?? 0;

                            return (
                              <tr key={item.productId}>
                                <td>
                                  <div className="table-cell-bold">{item.productName || `Product #${item.productId}`}</div>
                                  <div className="table-cell-muted">ID: #{item.productId}</div>
                                </td>
                                <td className="table-num table-cell-mono">{item.orderedQuantity ?? 0}</td>
                                <td className="table-num table-cell-mono">{item.receivedQuantity ?? 0}</td>
                                <td className="table-num table-cell-mono">
                                  <span
                                    style={{
                                      color: isFullyReceived ? "var(--emerald-600)" : "var(--primary)",
                                      fontWeight: 600,
                                    }}
                                  >
                                    {remaining}
                                  </span>
                                </td>
                                <td style={{ textAlign: "right" }}>
                                  <input
                                    type="number"
                                    min="0"
                                    max={remaining}
                                    step="1"
                                    disabled={isFullyReceived || isSubmitting || !isApproved}
                                    value={currentInputVal}
                                    onChange={(e) => handleQuantityChange(item.productId, e.target.value)}
                                    aria-label={`Receive quantity for ${item.productName || item.productId}`}
                                    style={{
                                      width: "100px",
                                      height: "30px",
                                      padding: "0 8px",
                                      border: "1px solid var(--border-strong)",
                                      borderRadius: "var(--radius)",
                                      fontSize: "0.82rem",
                                      textAlign: "right",
                                      background: isFullyReceived || !isApproved ? "var(--surface-subtle)" : "var(--surface)",
                                      color: isFullyReceived ? "var(--text-muted)" : "var(--text-primary)",
                                    }}
                                  />
                                </td>
                              </tr>
                            );
                          })
                        ) : (
                          <tr>
                            <td colSpan={5} style={{ textAlign: "center", padding: "18px 12px", color: "var(--text-muted)" }}>
                              No items found for this purchase order.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>

                {/* Receipt Header Fields */}
                <div className="form-grid">
                  <div className="form-group">
                    <label htmlFor="receive-date">
                      Received Date <span style={{ color: "var(--danger)" }}>*</span>
                    </label>
                    <input
                      id="receive-date"
                      type="date"
                      value={receivedDate}
                      onChange={(e) => {
                        setSubmitError("");
                        setReceivedDate(e.target.value);
                      }}
                      disabled={isSubmitting || !isApproved}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="receive-remarks">Remarks (Optional)</label>
                    <input
                      id="receive-remarks"
                      type="text"
                      placeholder="e.g. Items received in good condition"
                      value={remarks}
                      onChange={(e) => setRemarks(e.target.value)}
                      disabled={isSubmitting || !isApproved}
                    />
                  </div>
                </div>
              </>
            ) : null}
          </div>

          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={isSubmitting || !isApproved || isLoading || !details}
            >
              {isSubmitting ? "Submitting…" : "Submit Goods Receipt"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ReceiveItemsModal;
