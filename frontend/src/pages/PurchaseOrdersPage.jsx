import { useState, useEffect } from "react";
import { Search, FilePlus, RefreshCw, X, Plus, Trash2, CheckCircle, AlertTriangle } from "lucide-react";
import {
  getPurchaseOrders,
  getPurchaseOrderById,
  createPurchaseOrder,
  updatePurchaseOrderStatus,
  getVendors,
  getProducts,
} from "../services/api";
import { useAuth } from "../context/useAuth";
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
  const { user } = useAuth();
  const userRole = (user?.role || "").toUpperCase();
  const canUpdateStatus = userRole === "ADMIN" || userRole === "MANAGER";

  const [orders, setOrders]       = useState([]);
  const [vendors, setVendors]     = useState([]);
  const [products, setProducts]   = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [search, setSearch]       = useState("");

  // Create PO Modal state
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createError, setCreateError] = useState("");
  const [createForm, setCreateForm] = useState({
    vendorId: "",
    orderDate: new Date().toISOString().slice(0, 10),
    expectedDelivery: "",
    items: [{ productId: "", quantity: 1, unitPrice: "" }],
  });

  // View PO Details Modal state
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [selectedPO, setSelectedPO]     = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError]   = useState("");
  const [statusUpdating, setStatusUpdating] = useState(false);

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [poRes, vRes, pRes] = await Promise.all([
        getPurchaseOrders(),
        getVendors().catch(() => ({ data: [] })),
        getProducts().catch(() => ({ data: [] })),
      ]);

      const rawOrders = Array.isArray(poRes.data)
        ? poRes.data
        : (poRes.data?.data || []);
      const rawVendors = Array.isArray(vRes.data)
        ? vRes.data
        : (vRes.data?.data || []);
      const rawProducts = Array.isArray(pRes.data)
        ? pRes.data
        : (pRes.data?.data || []);

      const vendorsMap = {};
      rawVendors.forEach((v) => {
        vendorsMap[v.id] = v.vendorName || v.vendor_name;
      });

      const normalizedOrders = rawOrders.map((po) => ({
        id: po.id,
        po_number: po.poNumber || po.po_number || `PO-${po.id}`,
        vendor_id: po.vendorId,
        vendor_name: po.vendor_name || vendorsMap[po.vendorId] || `Vendor #${po.vendorId || "—"}`,
        order_date: po.orderDate || po.order_date,
        expected_delivery: po.expectedDelivery || po.expected_delivery,
        total_amount: po.totalAmount ?? po.total_amount,
        status: po.status || "Pending",
        created_by: po.createdBy,
        created_at: po.createdAt || po.created_at,
      }));

      setOrders(normalizedOrders);
      setVendors(rawVendors);
      setProducts(rawProducts);
    } catch {
      setError("Unable to load purchase orders.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  // Filtered orders
  const filtered = orders.filter(
    (po) =>
      po.po_number.toLowerCase().includes(search.toLowerCase()) ||
      (po.vendor_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (po.status ?? "").toLowerCase().includes(search.toLowerCase())
  );

  // ── Open View Details Modal ─────────────────────────────────
  const handleOpenDetail = async (poId) => {
    setDetailLoading(true);
    setDetailError("");
    setIsDetailOpen(true);
    try {
      const res = await getPurchaseOrderById(poId);
      const data = res.data?.data || res.data;
      setSelectedPO(data);
    } catch {
      setDetailError("Unable to fetch purchase order details.");
    } finally {
      setDetailLoading(false);
    }
  };

  // ── Status Update (Admin / Manager only) ──────────────────────
  const handleStatusUpdate = async (newStatus) => {
    if (!selectedPO?.id) return;
    setStatusUpdating(true);
    setDetailError("");
    try {
      await updatePurchaseOrderStatus(selectedPO.id, newStatus);
      setSelectedPO((prev) => ({ ...prev, status: newStatus }));
      setSuccessMsg(`Purchase order ${selectedPO.poNumber || selectedPO.po_number} status updated to ${newStatus}.`);
      loadData();
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403 ? "Forbidden: Only Admin and Manager roles can update status." : "Failed to update status.");
      setDetailError(msg);
    } finally {
      setStatusUpdating(false);
    }
  };

  // ── Create PO Form Helpers ──────────────────────────────────
  const handleAddItem = () => {
    setCreateForm((prev) => ({
      ...prev,
      items: [...prev.items, { productId: "", quantity: 1, unitPrice: "" }],
    }));
  };

  const handleRemoveItem = (index) => {
    setCreateForm((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index),
    }));
  };

  const handleItemChange = (index, field, value) => {
    setCreateForm((prev) => {
      const updated = [...prev.items];
      updated[index] = { ...updated[index], [field]: value };

      // If product changed, auto-populate unitPrice from product catalog
      if (field === "productId") {
        const prod = products.find((p) => String(p.id) === String(value));
        if (prod) {
          updated[index].unitPrice = prod.unitPrice ?? prod.unit_price ?? "";
        }
      }
      return { ...prev, items: updated };
    });
  };

  // Calculate total amount for create form
  const calculatedTotal = createForm.items.reduce((sum, item) => {
    const qty = Number(item.quantity) || 0;
    const price = Number(item.unitPrice) || 0;
    return sum + qty * price;
  }, 0);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreateError("");

    if (!createForm.vendorId) {
      setCreateError("Please select a vendor.");
      return;
    }
    if (createForm.items.length === 0) {
      setCreateError("Please add at least one line item.");
      return;
    }
    for (let i = 0; i < createForm.items.length; i++) {
      const it = createForm.items[i];
      if (!it.productId) {
        setCreateError(`Line item ${i + 1} requires a product selection.`);
        return;
      }
      if (Number(it.quantity) <= 0) {
        setCreateError(`Line item ${i + 1} quantity must be greater than 0.`);
        return;
      }
    }

    setCreateSubmitting(true);
    try {
      const payload = {
        vendorId: Number(createForm.vendorId),
        orderDate: createForm.orderDate,
        expectedDelivery: createForm.expectedDelivery || null,
        totalAmount: calculatedTotal,
        status: "Pending",
        items: createForm.items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          unitPrice: Number(it.unitPrice),
        })),
      };

      await createPurchaseOrder(payload);
      setSuccessMsg("Purchase order created successfully.");
      setIsCreateOpen(false);
      setCreateForm({
        vendorId: "",
        orderDate: new Date().toISOString().slice(0, 10),
        expectedDelivery: "",
        items: [{ productId: "", quantity: 1, unitPrice: "" }],
      });
      loadData();
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        "Failed to create purchase order. Please check inputs.";
      setCreateError(msg);
    } finally {
      setCreateSubmitting(false);
    }
  };

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
            <button className="btn btn-ghost btn-sm" onClick={() => { setLoading(true); loadData(); }} title="Refresh">
              <RefreshCw size={14} />
            </button>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setCreateError("");
                setIsCreateOpen(true);
              }}
            >
              <FilePlus size={14} />
              New PO
            </button>
          </div>
        </div>
      </div>

      {successMsg && (
        <div
          style={{
            background: "rgba(16, 185, 129, 0.12)",
            border: "1px solid rgba(16, 185, 129, 0.3)",
            borderRadius: "var(--radius)",
            padding: "10px 16px",
            color: "#059669",
            fontSize: "0.85rem",
            marginBottom: 18,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <CheckCircle size={16} />
            <span>{successMsg}</span>
          </div>
          <button
            onClick={() => setSuccessMsg("")}
            style={{ background: "none", border: "none", color: "inherit", cursor: "pointer" }}
          >
            <X size={14} />
          </button>
        </div>
      )}

      <div className="card">
        {error ? (
          <div className="empty-state">
            <p style={{ color: "var(--danger)", fontSize: "0.88rem" }}>{error}</p>
            <button className="btn btn-ghost btn-sm" onClick={() => { setLoading(true); loadData(); }} style={{ marginTop: 8 }}>
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
                      <button
                        className="btn btn-ghost btn-sm"
                        onClick={() => handleOpenDetail(po.id)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── CREATE PURCHASE ORDER MODAL ────────────────────────── */}
      {isCreateOpen && (
        <div className="modal-overlay" onClick={() => setIsCreateOpen(false)}>
          <div className="modal-dialog modal-dialog-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-title">Create Purchase Order</div>
              <button
                className="btn-ghost btn-sm"
                onClick={() => setIsCreateOpen(false)}
                style={{ padding: 4, borderRadius: "50%" }}
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleCreateSubmit}>
              <div className="modal-body">
                {createError && (
                  <div
                    style={{
                      background: "var(--danger-bg)",
                      border: "1px solid var(--danger-border)",
                      borderRadius: "var(--radius)",
                      padding: "10px 14px",
                      color: "var(--danger)",
                      fontSize: "0.82rem",
                      marginBottom: 16,
                    }}
                  >
                    {createError}
                  </div>
                )}

                <div className="form-grid">
                  <div className="form-group">
                    <label>Vendor *</label>
                    <select
                      value={createForm.vendorId}
                      onChange={(e) => setCreateForm({ ...createForm, vendorId: e.target.value })}
                      required
                    >
                      <option value="">Select a vendor…</option>
                      {vendors.map((v) => (
                        <option key={v.id} value={v.id}>
                          {v.vendorName || v.vendor_name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Order Date *</label>
                    <input
                      type="date"
                      value={createForm.orderDate}
                      onChange={(e) => setCreateForm({ ...createForm, orderDate: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Expected Delivery</label>
                    <input
                      type="date"
                      value={createForm.expectedDelivery}
                      onChange={(e) => setCreateForm({ ...createForm, expectedDelivery: e.target.value })}
                    />
                  </div>

                  <div className="form-group">
                    <label>Total Calculated</label>
                    <div style={{ padding: "8px 12px", fontWeight: 700, fontSize: "1rem", color: "var(--primary)" }}>
                      {fmtCurrency(calculatedTotal)}
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: 24 }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                    <div style={{ fontWeight: 600, fontSize: "0.9rem" }}>Line Items</div>
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={handleAddItem}
                    >
                      <Plus size={14} /> Add Item
                    </button>
                  </div>

                  <table className="table" style={{ background: "var(--bg)", borderRadius: "var(--radius)" }}>
                    <thead>
                      <tr>
                        <th>Product *</th>
                        <th style={{ width: 100 }}>Qty *</th>
                        <th style={{ width: 140 }}>Unit Price (₹) *</th>
                        <th style={{ width: 130 }}>Subtotal</th>
                        <th style={{ width: 50 }}></th>
                      </tr>
                    </thead>
                    <tbody>
                      {createForm.items.map((item, idx) => {
                        const subtotal = (Number(item.quantity) || 0) * (Number(item.unitPrice) || 0);
                        return (
                          <tr key={idx}>
                            <td>
                              <select
                                value={item.productId}
                                onChange={(e) => handleItemChange(idx, "productId", e.target.value)}
                                required
                                style={{ width: "100%", padding: "6px 8px", fontSize: "0.82rem" }}
                              >
                                <option value="">Select product…</option>
                                {products.map((p) => (
                                  <option key={p.id} value={p.id}>
                                    {p.productName || p.product_name}
                                  </option>
                                ))}
                              </select>
                            </td>
                            <td>
                              <input
                                type="number"
                                min="1"
                                value={item.quantity}
                                onChange={(e) => handleItemChange(idx, "quantity", e.target.value)}
                                required
                                style={{ width: "100%", padding: "6px 8px", fontSize: "0.82rem" }}
                              />
                            </td>
                            <td>
                              <input
                                type="number"
                                step="0.01"
                                min="0"
                                value={item.unitPrice}
                                onChange={(e) => handleItemChange(idx, "unitPrice", e.target.value)}
                                required
                                style={{ width: "100%", padding: "6px 8px", fontSize: "0.82rem" }}
                              />
                            </td>
                            <td style={{ fontWeight: 600 }}>
                              {fmtCurrency(subtotal)}
                            </td>
                            <td>
                              {createForm.items.length > 1 && (
                                <button
                                  type="button"
                                  onClick={() => handleRemoveItem(idx)}
                                  className="btn-ghost"
                                  style={{ color: "var(--danger)", border: "none", padding: 4 }}
                                  title="Remove item"
                                >
                                  <Trash2 size={15} />
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => setIsCreateOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={createSubmitting}
                >
                  {createSubmitting ? "Creating…" : "Create Purchase Order"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── VIEW PURCHASE ORDER DETAILS MODAL ──────────────────── */}
      {isDetailOpen && (
        <div className="modal-overlay" onClick={() => setIsDetailOpen(false)}>
          <div className="modal-dialog modal-dialog-lg" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <div className="modal-title">
                  Purchase Order: {selectedPO?.poNumber || selectedPO?.po_number || "Details"}
                </div>
                {selectedPO && (
                  <div style={{ marginTop: 4 }}>
                    <StatusBadge status={selectedPO.status} />
                  </div>
                )}
              </div>
              <button
                className="btn-ghost btn-sm"
                onClick={() => setIsDetailOpen(false)}
                style={{ padding: 4, borderRadius: "50%" }}
              >
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              {detailLoading ? (
                <LoadingState message="Loading details…" />
              ) : detailError ? (
                <div
                  style={{
                    background: "var(--danger-bg)",
                    border: "1px solid var(--danger-border)",
                    borderRadius: "var(--radius)",
                    padding: "10px 14px",
                    color: "var(--danger)",
                    fontSize: "0.85rem",
                    marginBottom: 16,
                  }}
                >
                  {detailError}
                </div>
              ) : selectedPO ? (
                <>
                  <div className="form-grid" style={{ marginBottom: 20 }}>
                    <div>
                      <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Order Date</div>
                      <div style={{ fontWeight: 600 }}>{fmt(selectedPO.orderDate || selectedPO.order_date)}</div>
                    </div>
                    <div>
                      <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Expected Delivery</div>
                      <div style={{ fontWeight: 600 }}>{fmt(selectedPO.expectedDelivery || selectedPO.expected_delivery)}</div>
                    </div>
                    <div>
                      <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Vendor</div>
                      <div style={{ fontWeight: 600 }}>
                        {selectedPO.vendor_name ||
                          vendors.find((v) => v.id === selectedPO.vendorId)?.vendorName ||
                          vendors.find((v) => v.id === selectedPO.vendorId)?.vendor_name ||
                          `Vendor #${selectedPO.vendorId || "—"}`}
                      </div>
                    </div>
                    <div>
                      <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Total Amount</div>
                      <div style={{ fontWeight: 700, fontSize: "1.1rem", color: "var(--primary)" }}>
                        {fmtCurrency(selectedPO.totalAmount ?? selectedPO.total_amount)}
                      </div>
                    </div>
                  </div>

                  {/* Status update section (Role-Based) */}
                  <div
                    style={{
                      background: "var(--bg)",
                      padding: "14px 18px",
                      borderRadius: "var(--radius)",
                      marginBottom: 20,
                      border: "1px solid var(--border)",
                    }}
                  >
                    <div style={{ fontSize: "0.85rem", fontWeight: 600, marginBottom: 8 }}>
                      Status Workflow:
                    </div>
                    {canUpdateStatus ? (
                      <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
                        <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
                          Change status to:
                        </span>
                        {["Pending", "Approved", "Rejected", "Completed"].map((st) => (
                          <button
                            key={st}
                            type="button"
                            className={`btn btn-sm ${selectedPO.status === st ? "btn-primary" : "btn-ghost"}`}
                            disabled={statusUpdating || selectedPO.status === st}
                            onClick={() => handleStatusUpdate(st)}
                          >
                            {st}
                          </button>
                        ))}
                      </div>
                    ) : (
                      <div style={{ fontSize: "0.8rem", color: "var(--text-muted)", display: "flex", alignItems: "center", gap: 6 }}>
                        <AlertTriangle size={14} />
                        Status updates are restricted to Admin and Manager accounts.
                      </div>
                    )}
                  </div>

                  {/* Line items table */}
                  <div>
                    <div style={{ fontWeight: 600, fontSize: "0.9rem", marginBottom: 10 }}>
                      Line Items ({selectedPO.items?.length || 0})
                    </div>
                    <table className="table">
                      <thead>
                        <tr>
                          <th>Product</th>
                          <th style={{ textAlign: "center" }}>Quantity</th>
                          <th style={{ textAlign: "right" }}>Unit Price</th>
                          <th style={{ textAlign: "right" }}>Total</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(selectedPO.items || []).map((item, idx) => {
                          const pId = item.productId || item.product_id || item.id?.productId;
                          const prod = products.find((p) => p.id === pId);
                          const pName = prod?.productName || prod?.product_name || item.product_name || `Product #${pId || idx + 1}`;
                          const qty = item.quantity;
                          const price = item.unitPrice ?? item.unit_price ?? 0;
                          const total = item.totalPrice ?? item.total_price ?? (qty * price);

                          return (
                            <tr key={idx}>
                              <td className="table-cell-bold">{pName}</td>
                              <td style={{ textAlign: "center" }}>{qty}</td>
                              <td style={{ textAlign: "right" }}>{fmtCurrency(price)}</td>
                              <td style={{ textAlign: "right", fontWeight: 600 }}>{fmtCurrency(total)}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </>
              ) : null}
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setIsDetailOpen(false)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default PurchaseOrdersPage;
