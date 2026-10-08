import { useState, useEffect } from "react";
import { Search, RefreshCw, X, Plus, Trash2, CheckCircle, AlertTriangle, Edit, Ban, Check, XCircle } from "lucide-react";
import {
  getPurchaseOrders,
  getPurchaseOrderById,
  createPurchaseOrder,
  updatePurchaseOrder,
  cancelPurchaseOrder,
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
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [vendorFilter, setVendorFilter] = useState("ALL");
  const [selectedRows, setSelectedRows] = useState(new Set());

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

  // Edit PO Modal state
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState("");
  const [editForm, setEditForm] = useState({
    id: null,
    poNumber: "",
    vendorId: "",
    orderDate: "",
    expectedDelivery: "",
    items: [{ productId: "", quantity: 1, unitPrice: "" }],
  });

  // Cancel PO Confirmation state
  const [poToCancel, setPoToCancel] = useState(null);
  const [cancelSubmitting, setCancelSubmitting] = useState(false);
  const [cancelError, setCancelError] = useState("");

  // View PO Details Modal state
  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [selectedPO, setSelectedPO]     = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError]   = useState("");
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [refreshKey, setRefreshKey]         = useState(0);

  const handleRefresh = () => {
    setLoading(true);
    setError("");
    setRefreshKey((k) => k + 1);
  };

  useEffect(() => {
    let ignore = false;
    const load = async () => {
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

        if (!ignore) {
          setOrders(normalizedOrders);
          setVendors(rawVendors);
          setProducts(rawProducts);
        }
      } catch {
        if (!ignore) {
          setError("Unable to load purchase orders.");
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

  // Tab counts for status filtering
  const pendingCount = orders.filter((o) => o.status === "Pending").length;
  const approvedCount = orders.filter((o) => o.status === "Approved").length;
  const completedCount = orders.filter((o) => o.status === "Completed").length;
  const rejectedCount = orders.filter((o) => o.status === "Rejected").length;
  const cancelledCount = orders.filter((o) => o.status === "Cancelled").length;

  const statusTabs = [
    { key: "ALL", label: "All Orders", count: orders.length },
    { key: "PENDING", label: "Pending", count: pendingCount },
    { key: "APPROVED", label: "Approved", count: approvedCount },
    { key: "COMPLETED", label: "Completed", count: completedCount },
    { key: "REJECTED", label: "Rejected", count: rejectedCount },
    { key: "CANCELLED", label: "Cancelled", count: cancelledCount },
  ];

  // Filtered orders
  const filtered = orders.filter((po) => {
    const matchesSearch =
      po.po_number.toLowerCase().includes(search.toLowerCase()) ||
      (po.vendor_name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (po.status ?? "").toLowerCase().includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === "ALL" || (po.status || "").toUpperCase() === statusFilter;
    const matchesVendor =
      vendorFilter === "ALL" || String(po.vendor_id) === String(vendorFilter);
    return matchesSearch && matchesStatus && matchesVendor;
  });

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedRows(new Set(filtered.map((o) => o.id)));
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
      setRefreshKey((k) => k + 1);
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

      if (field === "productId") {
        const prod = products.find((p) => String(p.id) === String(value));
        if (prod) {
          updated[index].unitPrice = prod.unitPrice ?? prod.unit_price ?? "";
        }
      }
      return { ...prev, items: updated };
    });
  };

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
      setRefreshKey((k) => k + 1);
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

  // ── Edit PO Form Helpers ────────────────────────────────────
  const handleOpenEdit = async (poId) => {
    setEditLoading(true);
    setEditError("");
    setIsEditOpen(true);
    try {
      const res = await getPurchaseOrderById(poId);
      const po = res.data?.data || res.data;
      if (po.status !== "Pending") {
        setEditError(`Only purchase orders with 'Pending' status can be edited. Current status: '${po.status}'.`);
        setEditForm({
          id: po.id,
          poNumber: po.poNumber || po.po_number || `PO-${po.id}`,
          vendorId: String(po.vendorId || po.vendor_id || ""),
          orderDate: (po.orderDate || po.order_date || "").slice(0, 10),
          expectedDelivery: (po.expectedDelivery || po.expected_delivery || "").slice(0, 10),
          items: [],
        });
        return;
      }

      const rawItems = po.items || [];
      const formattedItems = rawItems.map((item) => ({
        productId: String(item.productId || item.product_id || item.id?.productId || ""),
        quantity: item.quantity ?? 1,
        unitPrice: item.unitPrice ?? item.unit_price ?? "",
      }));

      setEditForm({
        id: po.id,
        poNumber: po.poNumber || po.po_number || `PO-${po.id}`,
        vendorId: String(po.vendorId || po.vendor_id || ""),
        orderDate: (po.orderDate || po.order_date || "").slice(0, 10),
        expectedDelivery: (po.expectedDelivery || po.expected_delivery || "").slice(0, 10),
        items: formattedItems.length > 0 ? formattedItems : [{ productId: "", quantity: 1, unitPrice: "" }],
      });
    } catch {
      setEditError("Unable to fetch purchase order details for editing.");
    } finally {
      setEditLoading(false);
    }
  };

  const handleEditAddItem = () => {
    setEditForm((prev) => ({
      ...prev,
      items: [...prev.items, { productId: "", quantity: 1, unitPrice: "" }],
    }));
  };

  const handleEditRemoveItem = (index) => {
    setEditForm((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index),
    }));
  };

  const handleEditItemChange = (index, field, value) => {
    setEditForm((prev) => {
      const updated = [...prev.items];
      updated[index] = { ...updated[index], [field]: value };

      if (field === "productId") {
        const prod = products.find((p) => String(p.id) === String(value));
        if (prod) {
          updated[index].unitPrice = prod.unitPrice ?? prod.unit_price ?? "";
        }
      }
      return { ...prev, items: updated };
    });
  };

  const calculatedEditTotal = editForm.items.reduce((sum, item) => {
    const qty = Number(item.quantity) || 0;
    const price = Number(item.unitPrice) || 0;
    return sum + qty * price;
  }, 0);

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditError("");

    if (!editForm.vendorId) {
      setEditError("Please select a vendor.");
      return;
    }
    if (!editForm.orderDate) {
      setEditError("Order date is required.");
      return;
    }
    if (editForm.expectedDelivery && editForm.expectedDelivery < editForm.orderDate) {
      setEditError("Expected delivery date cannot be before order date.");
      return;
    }
    if (!editForm.items || editForm.items.length === 0) {
      setEditError("Please add at least one line item.");
      return;
    }

    const pIds = editForm.items.map((it) => String(it.productId)).filter(Boolean);
    if (new Set(pIds).size !== pIds.length) {
      setEditError("Duplicate product selected. Each product can only appear once in a purchase order.");
      return;
    }

    for (let i = 0; i < editForm.items.length; i++) {
      const it = editForm.items[i];
      if (!it.productId) {
        setEditError(`Line item ${i + 1} requires a product selection.`);
        return;
      }
      const qty = Number(it.quantity);
      if (!qty || qty <= 0) {
        setEditError(`Line item ${i + 1} quantity must be greater than 0.`);
        return;
      }
      const price = Number(it.unitPrice);
      if (price === undefined || price === null || isNaN(price) || price <= 0) {
        setEditError(`Line item ${i + 1} unit price must be greater than 0.`);
        return;
      }
    }

    setEditSubmitting(true);
    try {
      const payload = {
        vendorId: Number(editForm.vendorId),
        orderDate: editForm.orderDate,
        expectedDelivery: editForm.expectedDelivery || null,
        items: editForm.items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          unitPrice: Number(it.unitPrice),
        })),
      };

      await updatePurchaseOrder(editForm.id, payload);
      setSuccessMsg(`Purchase order ${editForm.poNumber} updated successfully.`);
      setIsEditOpen(false);
      if (isDetailOpen) {
        setIsDetailOpen(false);
        setSelectedPO(null);
      }
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Forbidden: Only Admin and Manager roles can update purchase orders."
          : err.response?.status === 404
          ? "Purchase order not found."
          : "Failed to update purchase order. Please check inputs.");
      setEditError(msg);
    } finally {
      setEditSubmitting(false);
    }
  };

  // ── Cancel PO Helpers ───────────────────────────────────────
  const handleOpenCancel = (po) => {
    setPoToCancel({
      id: po.id,
      po_number: po.po_number || po.poNumber || `PO-${po.id}`,
      status: po.status,
    });
    setCancelError("");
  };

  const handleCloseCancel = () => {
    if (cancelSubmitting) return;
    setPoToCancel(null);
    setCancelError("");
  };

  const handleConfirmCancel = async () => {
    if (!poToCancel?.id || cancelSubmitting) return;

    setCancelSubmitting(true);
    setCancelError("");
    try {
      await cancelPurchaseOrder(poToCancel.id);
      const num = poToCancel.po_number;
      setSuccessMsg(`Purchase order ${num} has been cancelled successfully.`);
      setPoToCancel(null);
      if (selectedPO && selectedPO.id === poToCancel.id) {
        setSelectedPO((prev) => ({ ...prev, status: "Cancelled" }));
      }
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Forbidden: Only Admin and Manager roles can cancel purchase orders."
          : err.response?.status === 404
          ? "Purchase order not found."
          : "Failed to cancel purchase order.");
      setCancelError(msg);
    } finally {
      setCancelSubmitting(false);
    }
  };

  return (
    <>
      {/* Enterprise Page Header */}
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Purchase Orders</div>
            <div className="page-subtitle">
              Manage procurement orders, monitor deliveries, and control status workflows.
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} title="Refresh records">
              <RefreshCw size={13} />
              <span>Refresh</span>
            </button>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setCreateError("");
                setIsCreateOpen(true);
              }}
            >
              <Plus size={14} />
              <span>Create Purchase Order</span>
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

      {/* Reference C Status Segmented Bar */}
      <div style={{ marginBottom: 12 }}>
        <div className="filter-tabs" role="tablist" aria-label="Order status filter">
          {statusTabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              className={`filter-tab ${statusFilter === tab.key ? "active" : ""}`}
              onClick={() => setStatusFilter(tab.key)}
              role="tab"
              aria-selected={statusFilter === tab.key}
            >
              <span>{tab.label}</span>
              <span className="filter-tab-count">{tab.count}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Main Table Card */}
      <div className="card">
        {/* Dedicated Control Toolbar (Reference C) */}
        <div className="table-toolbar">
          <div className="table-toolbar-left">
            <div className="search-box">
              <Search className="search-box-icon" size={14} />
              <input
                type="search"
                placeholder="Search orders, vendors..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                aria-label="Search purchase orders"
              />
            </div>

            <div className="table-filter-group">
              <select
                className="table-filter-select"
                value={vendorFilter}
                onChange={(e) => setVendorFilter(e.target.value)}
                aria-label="Filter by vendor"
              >
                <option value="ALL">Vendor: All Vendors</option>
                {vendors.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.vendorName || v.vendor_name}
                  </option>
                ))}
              </select>

              <select
                className="table-filter-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                aria-label="Filter by status"
              >
                <option value="ALL">Status: All Statuses</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="COMPLETED">Completed</option>
                <option value="REJECTED">Rejected</option>
                <option value="CANCELLED">Cancelled</option>
              </select>

              <button
                type="button"
                className="table-filter-btn"
                title="Add custom filter"
              >
                <Plus size={12} />
                <span>Add filter</span>
              </button>

              {(search || vendorFilter !== "ALL" || statusFilter !== "ALL") && (
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  style={{ fontSize: "0.74rem", padding: "3px 8px" }}
                  onClick={() => {
                    setSearch("");
                    setVendorFilter("ALL");
                    setStatusFilter("ALL");
                  }}
                >
                  Clear filters
                </button>
              )}
            </div>
          </div>

          <div className="table-toolbar-right">
            <span style={{ fontSize: "0.74rem", color: "var(--text-muted)" }}>
              Showing {filtered.length} of {orders.length} orders
            </span>
            <button
              className="btn btn-ghost btn-sm btn-icon"
              onClick={handleRefresh}
              title="Refresh records"
              aria-label="Refresh records"
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
          <LoadingState message="Loading purchase orders…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No purchase orders found"
            description={search || vendorFilter !== "ALL" || statusFilter !== "ALL" ? "No orders match the current filter or search criteria." : "No purchase orders have been created yet."}
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
                        aria-label="Select all orders"
                      />
                    </th>
                    <th>PO Number</th>
                    <th>Vendor</th>
                    <th>Order Date</th>
                    <th>Expected Delivery</th>
                    <th style={{ textAlign: "right" }}>Total Amount</th>
                    <th>Status</th>
                    <th style={{ textAlign: "right" }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((po) => {
                    const isSelected = selectedRows.has(po.id);
                    return (
                      <tr key={po.id} className={isSelected ? "row-selected" : ""}>
                        <td style={{ textAlign: "center" }}>
                          <input
                            type="checkbox"
                            className="table-checkbox"
                            checked={isSelected}
                            onChange={() => handleSelectRow(po.id)}
                            aria-label={`Select order ${po.po_number}`}
                          />
                        </td>
                        <td className="table-cell-mono table-cell-bold">{po.po_number}</td>
                        <td>{po.vendor_name ?? "—"}</td>
                        <td className="table-cell-muted">{fmt(po.order_date)}</td>
                        <td className="table-cell-muted">{fmt(po.expected_delivery)}</td>
                        <td className="table-num" style={{ fontWeight: 600 }}>{fmtCurrency(po.total_amount)}</td>
                        <td><StatusBadge status={po.status} /></td>
                        <td style={{ textAlign: "right" }}>
                          <div style={{ display: "inline-flex", gap: 6, alignItems: "center", justifyContent: "flex-end" }}>
                            <button
                              type="button"
                              className="btn btn-ghost btn-sm"
                              onClick={() => handleOpenDetail(po.id)}
                              style={{ padding: "3px 8px" }}
                              title="View Details"
                            >
                              View
                            </button>
                            {canUpdateStatus && po.status === "Pending" && (
                              <button
                                type="button"
                                className="btn btn-ghost btn-sm"
                                onClick={() => handleOpenEdit(po.id)}
                                style={{ gap: 4, padding: "3px 8px" }}
                                title="Edit Purchase Order"
                                id={`edit-po-${po.id}-btn`}
                              >
                                <Edit size={12} />
                                <span>Edit</span>
                              </button>
                            )}
                            {canUpdateStatus && (po.status === "Pending" || po.status === "Approved") && (
                              <button
                                type="button"
                                className="btn btn-ghost btn-sm"
                                onClick={() => handleOpenCancel(po)}
                                style={{ gap: 4, padding: "3px 8px", color: "var(--danger)" }}
                                title="Cancel Purchase Order"
                                id={`cancel-po-${po.id}-btn`}
                              >
                                <Ban size={12} />
                                <span>Cancel</span>
                              </button>
                            )}
                          </div>
                        </td>
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

      {/* ── CREATE PURCHASE ORDER MODAL ────────────────────────── */}
      {isCreateOpen && (
        <div className="modal-overlay" onClick={() => setIsCreateOpen(false)}>
          <div className="modal-dialog modal-dialog-lg" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="create-po-title">
            <div className="modal-header">
              <div id="create-po-title" className="modal-title">Create Purchase Order</div>
              <button
                className="btn btn-ghost btn-sm btn-icon"
                onClick={() => setIsCreateOpen(false)}
                aria-label="Close"
              >
                <X size={15} />
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
                      padding: "9px 12px",
                      color: "var(--danger)",
                      fontSize: "0.8rem",
                      marginBottom: 14,
                    }}
                    role="alert"
                  >
                    {createError}
                  </div>
                )}

                <div className="modal-section-title">PO Information</div>
                <div className="form-grid">
                  <div className="form-group">
                    <label htmlFor="po-vendor-select">Vendor *</label>
                    <select
                      id="po-vendor-select"
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
                    <label htmlFor="po-order-date">Order Date *</label>
                    <input
                      id="po-order-date"
                      type="date"
                      value={createForm.orderDate}
                      onChange={(e) => setCreateForm({ ...createForm, orderDate: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="po-delivery-date">Expected Delivery</label>
                    <input
                      id="po-delivery-date"
                      type="date"
                      value={createForm.expectedDelivery}
                      onChange={(e) => setCreateForm({ ...createForm, expectedDelivery: e.target.value })}
                    />
                  </div>
                </div>

                <div style={{ marginTop: 22 }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
                    <div className="modal-section-title" style={{ marginBottom: 0 }}>
                      Line Items ({createForm.items.length})
                    </div>
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={handleAddItem}
                    >
                      <Plus size={13} /> Add Item
                    </button>
                  </div>

                  <div className="table-container" style={{ border: "1px solid var(--border)", borderRadius: "var(--radius)" }}>
                    <table className="table">
                      <thead>
                        <tr>
                          <th>Product *</th>
                          <th style={{ width: 90, textAlign: "center" }}>Qty *</th>
                          <th style={{ width: 130, textAlign: "right" }}>Unit Price (₹) *</th>
                          <th style={{ width: 130, textAlign: "right" }}>Subtotal</th>
                          <th style={{ width: 44, textAlign: "center" }}></th>
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
                                  style={{ width: "100%", height: 28, fontSize: "0.78rem" }}
                                  aria-label={`Product for line item ${idx + 1}`}
                                >
                                  <option value="">Select product…</option>
                                  {products.map((p) => (
                                    <option key={p.id} value={p.id}>
                                      {p.productName || p.product_name}
                                    </option>
                                  ))}
                                </select>
                              </td>
                              <td style={{ textAlign: "center" }}>
                                <input
                                  type="number"
                                  min="1"
                                  value={item.quantity}
                                  onChange={(e) => handleItemChange(idx, "quantity", e.target.value)}
                                  required
                                  style={{ width: 70, height: 28, textAlign: "center", fontSize: "0.78rem" }}
                                  aria-label={`Quantity for line item ${idx + 1}`}
                                />
                              </td>
                              <td style={{ textAlign: "right" }}>
                                <input
                                  type="number"
                                  step="0.01"
                                  min="0"
                                  value={item.unitPrice}
                                  onChange={(e) => handleItemChange(idx, "unitPrice", e.target.value)}
                                  required
                                  style={{ width: 110, height: 28, textAlign: "right", fontSize: "0.78rem" }}
                                  aria-label={`Unit price for line item ${idx + 1}`}
                                />
                              </td>
                              <td className="table-num" style={{ fontWeight: 600 }}>
                                {fmtCurrency(subtotal)}
                              </td>
                              <td style={{ textAlign: "center" }}>
                                {createForm.items.length > 1 && (
                                  <button
                                    type="button"
                                    onClick={() => handleRemoveItem(idx)}
                                    className="btn btn-ghost btn-sm btn-icon"
                                    style={{ color: "var(--danger)", border: "none" }}
                                    title="Remove item"
                                    aria-label="Remove item"
                                  >
                                    <Trash2 size={14} />
                                  </button>
                                )}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>

                  {/* Financial Summary Card */}
                  <div className="modal-totals-card">
                    <div className="modal-totals-box">
                      <div className="modal-totals-row">
                        <span>Items Subtotal:</span>
                        <span>{fmtCurrency(calculatedTotal)}</span>
                      </div>
                      <div className="modal-totals-row">
                        <span>Estimated Tax (0%):</span>
                        <span>₹0.00</span>
                      </div>
                      <div className="modal-totals-divider" />
                      <div className="modal-totals-row grand-total">
                        <span>Grand Total:</span>
                        <span>{fmtCurrency(calculatedTotal)}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => setIsCreateOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm"
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
          <div className="modal-dialog modal-dialog-lg" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="view-po-title">
            <div className="modal-header">
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <div id="view-po-title" className="modal-title">
                  Purchase Order: {selectedPO?.poNumber || selectedPO?.po_number || "Details"}
                </div>
                {selectedPO && <StatusBadge status={selectedPO.status} />}
              </div>
              <button
                className="btn btn-ghost btn-sm btn-icon"
                onClick={() => setIsDetailOpen(false)}
                aria-label="Close"
              >
                <X size={15} />
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
                    padding: "9px 12px",
                    color: "var(--danger)",
                    fontSize: "0.82rem",
                    marginBottom: 14,
                  }}
                  role="alert"
                >
                  {detailError}
                </div>
              ) : selectedPO ? (
                <>
                  {/* Order Information Section */}
                  <div className="modal-section-title">Order Information</div>
                  <div className="summary-strip">
                    <div className="summary-tile">
                      <span className="summary-tile-label">Order Date</span>
                      <span className="summary-tile-value">{fmt(selectedPO.orderDate || selectedPO.order_date)}</span>
                    </div>
                    <div className="summary-tile">
                      <span className="summary-tile-label">Expected Delivery</span>
                      <span className="summary-tile-value">{fmt(selectedPO.expectedDelivery || selectedPO.expected_delivery)}</span>
                    </div>
                    <div className="summary-tile">
                      <span className="summary-tile-label">Vendor</span>
                      <span className="summary-tile-value" style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {selectedPO.vendor_name ||
                          vendors.find((v) => v.id === selectedPO.vendorId)?.vendorName ||
                          vendors.find((v) => v.id === selectedPO.vendorId)?.vendor_name ||
                          `Vendor #${selectedPO.vendorId || "—"}`}
                      </span>
                    </div>
                    <div className="summary-tile">
                      <span className="summary-tile-label">Total Amount</span>
                      <span className="summary-tile-value" style={{ color: "var(--primary)" }}>
                        {fmtCurrency(selectedPO.totalAmount ?? selectedPO.total_amount)}
                      </span>
                    </div>
                  </div>

                  {/* Status Workflow Section (Role-Based) */}
                  <div className="modal-section-title">Status Workflow</div>
                  <div
                    style={{
                      background: "var(--surface-subtle)",
                      padding: "12px 14px",
                      borderRadius: "var(--radius)",
                      marginBottom: 18,
                      border: "1px solid var(--border)",
                    }}
                  >
                    {canUpdateStatus ? (
                      <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
                        {selectedPO.status === "Pending" && (
                          <>
                            <button
                              type="button"
                              className="btn btn-sm btn-ghost"
                              style={{ gap: 4 }}
                              onClick={() => {
                                setIsDetailOpen(false);
                                handleOpenEdit(selectedPO.id);
                              }}
                              title="Edit Purchase Order"
                              id="detail-edit-po-btn"
                            >
                              <Edit size={13} />
                              <span>Edit Order</span>
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-primary"
                              style={{ gap: 4 }}
                              disabled={statusUpdating}
                              onClick={() => handleStatusUpdate("Approved")}
                              id="detail-approve-po-btn"
                            >
                              <Check size={13} />
                              <span>Approve</span>
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-danger"
                              style={{ gap: 4 }}
                              disabled={statusUpdating}
                              onClick={() => handleStatusUpdate("Rejected")}
                              id="detail-reject-po-btn"
                            >
                              <XCircle size={13} />
                              <span>Reject</span>
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-danger"
                              style={{ gap: 4 }}
                              disabled={statusUpdating}
                              onClick={() => handleOpenCancel(selectedPO)}
                              id="detail-cancel-po-btn"
                            >
                              <Ban size={13} />
                              <span>Cancel Order</span>
                            </button>
                          </>
                        )}

                        {selectedPO.status === "Approved" && (
                          <>
                            <button
                              type="button"
                              className="btn btn-sm btn-primary"
                              style={{ gap: 4 }}
                              disabled={statusUpdating}
                              onClick={() => handleStatusUpdate("Completed")}
                              id="detail-complete-po-btn"
                            >
                              <Check size={13} />
                              <span>Complete</span>
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-danger"
                              style={{ gap: 4 }}
                              disabled={statusUpdating}
                              onClick={() => handleOpenCancel(selectedPO)}
                              id="detail-cancel-po-btn"
                            >
                              <Ban size={13} />
                              <span>Cancel Order</span>
                            </button>
                          </>
                        )}

                        {(selectedPO.status === "Rejected" ||
                          selectedPO.status === "Completed" ||
                          selectedPO.status === "Cancelled") && (
                          <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
                            This purchase order is in a terminal state <strong>({selectedPO.status})</strong>. No further actions can be taken.
                          </span>
                        )}
                      </div>
                    ) : (
                      <div style={{ fontSize: "0.76rem", color: "var(--text-muted)", display: "flex", alignItems: "center", gap: 6 }}>
                        <AlertTriangle size={13} />
                        <span>Status updates and modifications are restricted to Admin and Manager accounts.</span>
                      </div>
                    )}
                  </div>

                  {/* Line Items Table Section */}
                  <div>
                    <div className="modal-section-title">
                      Line Items ({selectedPO.items?.length || 0})
                    </div>
                    <div className="table-container" style={{ border: "1px solid var(--border)", borderRadius: "var(--radius)" }}>
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
                                <td className="table-num">{fmtCurrency(price)}</td>
                                <td className="table-num" style={{ fontWeight: 600 }}>{fmtCurrency(total)}</td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>

                    {/* Order Financial Totals */}
                    <div className="modal-totals-card">
                      <div className="modal-totals-box">
                        <div className="modal-totals-row">
                          <span>Items Subtotal:</span>
                          <span>{fmtCurrency(selectedPO.totalAmount ?? selectedPO.total_amount)}</span>
                        </div>
                        <div className="modal-totals-divider" />
                        <div className="modal-totals-row grand-total">
                          <span>Grand Total:</span>
                          <span>{fmtCurrency(selectedPO.totalAmount ?? selectedPO.total_amount)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </>
              ) : null}
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => setIsDetailOpen(false)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── EDIT PURCHASE ORDER MODAL ────────────────────────── */}
      {isEditOpen && (
        <div className="modal-overlay" onClick={() => !editSubmitting && setIsEditOpen(false)}>
          <div
            className="modal-dialog modal-dialog-lg"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="edit-po-title"
          >
            <div className="modal-header">
              <div id="edit-po-title" className="modal-title">
                Edit Purchase Order: {editForm.poNumber}
              </div>
              <button
                type="button"
                className="btn btn-ghost btn-sm btn-icon"
                onClick={() => setIsEditOpen(false)}
                disabled={editSubmitting}
                aria-label="Close"
              >
                <X size={15} />
              </button>
            </div>

            {editLoading ? (
              <div className="modal-body">
                <LoadingState message="Loading purchase order details…" />
              </div>
            ) : (
              <form onSubmit={handleEditSubmit}>
                <div className="modal-body">
                  {editError && (
                    <div
                      style={{
                        background: "var(--danger-bg)",
                        border: "1px solid var(--danger-border)",
                        borderRadius: "var(--radius)",
                        padding: "9px 12px",
                        color: "var(--danger)",
                        fontSize: "0.8rem",
                        marginBottom: 14,
                      }}
                      role="alert"
                    >
                      {editError}
                    </div>
                  )}

                  <div className="modal-section-title">PO Information</div>
                  <div className="form-grid">
                    <div className="form-group">
                      <label htmlFor="edit-po-vendor-select">Vendor *</label>
                      <select
                        id="edit-po-vendor-select"
                        value={editForm.vendorId}
                        onChange={(e) => setEditForm({ ...editForm, vendorId: e.target.value })}
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
                      <label htmlFor="edit-po-order-date">Order Date *</label>
                      <input
                        id="edit-po-order-date"
                        type="date"
                        value={editForm.orderDate}
                        onChange={(e) => setEditForm({ ...editForm, orderDate: e.target.value })}
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="edit-po-delivery-date">Expected Delivery</label>
                      <input
                        id="edit-po-delivery-date"
                        type="date"
                        value={editForm.expectedDelivery}
                        onChange={(e) => setEditForm({ ...editForm, expectedDelivery: e.target.value })}
                      />
                    </div>
                  </div>

                  <div style={{ marginTop: 22 }}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
                      <div className="modal-section-title" style={{ marginBottom: 0 }}>
                        Line Items ({editForm.items.length})
                      </div>
                      <button
                        type="button"
                        className="btn btn-ghost btn-sm"
                        onClick={handleEditAddItem}
                      >
                        <Plus size={13} /> Add Item
                      </button>
                    </div>

                    <div className="table-container" style={{ border: "1px solid var(--border)", borderRadius: "var(--radius)" }}>
                      <table className="table">
                        <thead>
                          <tr>
                            <th>Product *</th>
                            <th style={{ width: 90, textAlign: "center" }}>Qty *</th>
                            <th style={{ width: 130, textAlign: "right" }}>Unit Price (₹) *</th>
                            <th style={{ width: 130, textAlign: "right" }}>Subtotal</th>
                            <th style={{ width: 44, textAlign: "center" }}></th>
                          </tr>
                        </thead>
                        <tbody>
                          {editForm.items.map((item, idx) => {
                            const subtotal = (Number(item.quantity) || 0) * (Number(item.unitPrice) || 0);
                            return (
                              <tr key={idx}>
                                <td>
                                  <select
                                    value={item.productId}
                                    onChange={(e) => handleEditItemChange(idx, "productId", e.target.value)}
                                    required
                                    style={{ width: "100%", height: 28, fontSize: "0.78rem" }}
                                    aria-label={`Product for line item ${idx + 1}`}
                                  >
                                    <option value="">Select product…</option>
                                    {products.map((p) => {
                                      const isSelectedElsewhere = editForm.items.some(
                                        (other, oIdx) => oIdx !== idx && String(other.productId) === String(p.id)
                                      );
                                      return (
                                        <option key={p.id} value={p.id} disabled={isSelectedElsewhere}>
                                          {p.productName || p.product_name} {isSelectedElsewhere ? "(Already added)" : ""}
                                        </option>
                                      );
                                    })}
                                  </select>
                                </td>
                                <td style={{ textAlign: "center" }}>
                                  <input
                                    type="number"
                                    min="1"
                                    value={item.quantity}
                                    onChange={(e) => handleEditItemChange(idx, "quantity", e.target.value)}
                                    required
                                    style={{ width: 70, height: 28, textAlign: "center", fontSize: "0.78rem" }}
                                    aria-label={`Quantity for line item ${idx + 1}`}
                                  />
                                </td>
                                <td style={{ textAlign: "right" }}>
                                  <input
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    value={item.unitPrice}
                                    onChange={(e) => handleEditItemChange(idx, "unitPrice", e.target.value)}
                                    required
                                    style={{ width: 110, height: 28, textAlign: "right", fontSize: "0.78rem" }}
                                    aria-label={`Unit price for line item ${idx + 1}`}
                                  />
                                </td>
                                <td className="table-num" style={{ fontWeight: 600 }}>
                                  {fmtCurrency(subtotal)}
                                </td>
                                <td style={{ textAlign: "center" }}>
                                  {editForm.items.length > 1 && (
                                    <button
                                      type="button"
                                      onClick={() => handleEditRemoveItem(idx)}
                                      className="btn btn-ghost btn-sm btn-icon"
                                      style={{ color: "var(--danger)", border: "none" }}
                                      title="Remove item"
                                      aria-label="Remove item"
                                    >
                                      <Trash2 size={14} />
                                    </button>
                                  )}
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>

                    {/* Financial Summary Card */}
                    <div className="modal-totals-card">
                      <div className="modal-totals-box">
                        <div className="modal-totals-row">
                          <span>Items Subtotal:</span>
                          <span>{fmtCurrency(calculatedEditTotal)}</span>
                        </div>
                        <div className="modal-totals-row">
                          <span>Estimated Tax (0%):</span>
                          <span>₹0.00</span>
                        </div>
                        <div className="modal-totals-divider" />
                        <div className="modal-totals-row grand-total">
                          <span>Grand Total:</span>
                          <span>{fmtCurrency(calculatedEditTotal)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="modal-footer">
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    onClick={() => setIsEditOpen(false)}
                    disabled={editSubmitting}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn btn-primary btn-sm"
                    disabled={editSubmitting}
                    id="submit-edit-po-btn"
                  >
                    {editSubmitting ? "Saving…" : "Save Changes"}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* ── CANCEL PURCHASE ORDER CONFIRMATION MODAL ───────────── */}
      {poToCancel && (
        <div
          className="modal-overlay"
          onClick={handleCloseCancel}
        >
          <div
            className="modal-dialog"
            style={{ maxWidth: 480 }}
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="cancel-po-title"
          >
            <div className="modal-header">
              <div id="cancel-po-title" className="modal-title" style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <AlertTriangle size={16} style={{ color: "var(--danger)" }} />
                <span>Cancel Purchase Order</span>
              </div>
              <button
                type="button"
                className="btn btn-ghost btn-sm btn-icon"
                onClick={handleCloseCancel}
                disabled={cancelSubmitting}
                aria-label="Close"
              >
                <X size={15} />
              </button>
            </div>

            <div className="modal-body">
              {cancelError && (
                <div
                  style={{
                    background: "var(--danger-bg)",
                    border: "1px solid var(--danger-border)",
                    borderRadius: "var(--radius)",
                    padding: "9px 12px",
                    color: "var(--danger)",
                    fontSize: "0.8rem",
                    marginBottom: 14,
                  }}
                  role="alert"
                >
                  {cancelError}
                </div>
              )}

              <p style={{ margin: "0 0 12px", fontSize: "0.875rem", color: "var(--text-primary)", lineHeight: 1.5 }}>
                Are you sure you want to cancel purchase order <strong>"{poToCancel.po_number}"</strong>?
              </p>

              <div
                style={{
                  background: "var(--background)",
                  border: "1px solid var(--border)",
                  borderRadius: "var(--radius)",
                  padding: "10px 14px",
                  fontSize: "0.8rem",
                  color: "var(--text-secondary)",
                  lineHeight: 1.5,
                }}
              >
                <div style={{ fontWeight: 600, color: "var(--text-primary)", marginBottom: 4 }}>
                  Business Behavior:
                </div>
                <ul style={{ margin: 0, paddingLeft: 18 }}>
                  <li>The purchase order status will be updated to <strong>Cancelled</strong>.</li>
                  <li>The purchase order <strong>will not be deleted</strong> and will remain in the database for auditing and history.</li>
                  <li>Cancelled is a terminal state; no further status transitions or edits will be permitted.</li>
                </ul>
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={handleCloseCancel}
                disabled={cancelSubmitting}
              >
                Close
              </button>
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={handleConfirmCancel}
                disabled={cancelSubmitting}
                id="confirm-cancel-po-btn"
              >
                {cancelSubmitting ? (
                  <>
                    <RefreshCw size={13} className="spin" />
                    <span>Cancelling…</span>
                  </>
                ) : (
                  <>
                    <Ban size={13} />
                    <span>Cancel Purchase Order</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default PurchaseOrdersPage;
