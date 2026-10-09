import { useState, useEffect } from "react";
import { Search, PackagePlus, RefreshCw, Plus, X, Edit, Ban, AlertTriangle } from "lucide-react";
import { getProducts, getVendors, createProduct, updateProduct, deactivateProduct } from "../services/api";
import { useAuth } from "../context/useAuth";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

function fmtCurrency(amount) {
  if (amount == null) return "—";
  return "₹" + Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2 });
}

const initialCreateForm = {
  vendorId: "",
  productName: "",
  category: "",
  description: "",
  unitPrice: "",
  stockQuantity: 0,
  unit: "",
  status: "Available",
};

const initialEditForm = {
  id: null,
  vendorId: "",
  productName: "",
  category: "",
  description: "",
  unitPrice: "",
  stockQuantity: 0,
  unit: "",
  status: "Available",
};

function ProductsPage() {
  const { canManageProducts } = useAuth();
  const [products, setProducts]       = useState([]);
  const [vendors, setVendors]         = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState("");
  const [search, setSearch]           = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [refreshKey, setRefreshKey]   = useState(0);
  const [selectedRows, setSelectedRows] = useState(new Set());

  // Create Product modal state
  const [isCreateOpen, setIsCreateOpen]         = useState(false);
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createError, setCreateError]           = useState("");
  const [successMsg, setSuccessMsg]             = useState("");
  const [createForm, setCreateForm]             = useState(initialCreateForm);

  // Edit Product modal state
  const [isEditOpen, setIsEditOpen]             = useState(false);
  const [editSubmitting, setEditSubmitting]     = useState(false);
  const [editError, setEditError]               = useState("");
  const [editForm, setEditForm]                 = useState(initialEditForm);

  // Deactivate Product modal state
  const [productToDeactivate, setProductToDeactivate] = useState(null);
  const [deactivateSubmitting, setDeactivateSubmitting] = useState(false);
  const [deactivateError, setDeactivateError]           = useState("");

  const handleRefresh = () => {
    setLoading(true);
    setError("");
    setRefreshKey((k) => k + 1);
  };

  const handleOpenCreate = () => {
    setCreateForm(initialCreateForm);
    setCreateError("");
    setIsCreateOpen(true);
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreateError("");

    const trimmedName = createForm.productName?.trim();
    if (!trimmedName) {
      setCreateError("Product name is required.");
      return;
    }

    if (!createForm.vendorId) {
      setCreateError("Please select a supplier vendor.");
      return;
    }

    const priceNum = Number(createForm.unitPrice);
    if (createForm.unitPrice === "" || isNaN(priceNum) || priceNum < 0) {
      setCreateError("Unit price must be a valid non-negative number.");
      return;
    }

    const stockNum = Number(createForm.stockQuantity);
    if (createForm.stockQuantity !== "" && (isNaN(stockNum) || stockNum < 0)) {
      setCreateError("Stock quantity cannot be negative.");
      return;
    }

    setCreateSubmitting(true);
    try {
      const payload = {
        vendorId: Number(createForm.vendorId),
        productName: trimmedName,
        category: createForm.category?.trim() || null,
        description: createForm.description?.trim() || null,
        unitPrice: priceNum,
        stockQuantity: isNaN(stockNum) ? 0 : Math.floor(stockNum),
        unit: createForm.unit?.trim() || null,
        status: createForm.status || "Available",
      };

      const res = await createProduct(payload);
      const created = res.data;
      setSuccessMsg(`Product "${created.productName || created.product_name || trimmedName}" created successfully.`);
      setIsCreateOpen(false);
      setCreateForm(initialCreateForm);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Access denied: Only Admin and Manager roles can add products."
          : "Failed to create product. Please check inputs.");
      setCreateError(msg);
    } finally {
      setCreateSubmitting(false);
    }
  };

  const handleOpenEdit = (p) => {
    setEditForm({
      id: p.id,
      vendorId: p.vendor_id ?? (p.vendorId || ""),
      productName: p.rawProductName || (p.product_name !== "—" ? p.product_name : "") || "",
      category: p.rawCategory || (p.category !== "—" ? p.category : "") || "",
      description: p.rawDescription || (p.description !== "—" ? p.description : "") || "",
      unitPrice: p.unit_price != null ? p.unit_price : "",
      stockQuantity: p.stock_quantity != null ? p.stock_quantity : 0,
      unit: p.rawUnit || (p.unit !== "—" ? p.unit : "") || "",
      status: p.status || "Available",
    });
    setEditError("");
    setIsEditOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditError("");

    const trimmedName = editForm.productName?.trim();
    if (!trimmedName) {
      setEditError("Product name is required.");
      return;
    }

    if (!editForm.vendorId) {
      setEditError("Please select a supplier vendor.");
      return;
    }

    const priceNum = Number(editForm.unitPrice);
    if (editForm.unitPrice === "" || isNaN(priceNum) || priceNum < 0) {
      setEditError("Unit price must be a valid non-negative number.");
      return;
    }

    const stockNum = Number(editForm.stockQuantity);
    if (editForm.stockQuantity !== "" && (isNaN(stockNum) || stockNum < 0)) {
      setEditError("Stock quantity cannot be negative.");
      return;
    }

    setEditSubmitting(true);
    try {
      const payload = {
        vendorId: Number(editForm.vendorId),
        productName: trimmedName,
        category: editForm.category?.trim() || null,
        description: editForm.description?.trim() || null,
        unitPrice: priceNum,
        stockQuantity: isNaN(stockNum) ? 0 : Math.floor(stockNum),
        unit: editForm.unit?.trim() || null,
        status: editForm.status || "Available",
      };

      const res = await updateProduct(editForm.id, payload);
      const updated = res.data;
      setSuccessMsg(`Product "${updated.productName || updated.product_name || trimmedName}" updated successfully.`);
      setIsEditOpen(false);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Access denied: Only Admin and Manager roles can update products."
          : err.response?.status === 404
          ? "Product not found."
          : "Failed to update product. Please check inputs.");
      setEditError(msg);
    } finally {
      setEditSubmitting(false);
    }
  };

  const handleOpenDeactivate = (product) => {
    setProductToDeactivate(product);
    setDeactivateError("");
  };

  const handleCloseDeactivate = () => {
    if (deactivateSubmitting) return;
    setProductToDeactivate(null);
    setDeactivateError("");
  };

  const handleConfirmDeactivate = async () => {
    if (!productToDeactivate || deactivateSubmitting) return;

    setDeactivateSubmitting(true);
    setDeactivateError("");
    try {
      await deactivateProduct(productToDeactivate.id);
      const prodName =
        productToDeactivate.product_name ||
        productToDeactivate.rawProductName ||
        `Product #${productToDeactivate.id}`;
      setSuccessMsg(`Product "${prodName}" deactivated successfully.`);
      setProductToDeactivate(null);
      // Immediately reflect status as Unavailable in local state
      setProducts((prev) =>
        prev.map((p) =>
          p.id === productToDeactivate.id ? { ...p, status: "Unavailable" } : p
        )
      );
      // Trigger background catalog sync
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Access denied: Only Admin and Manager roles can deactivate products."
          : err.response?.status === 404
          ? "Product not found."
          : "Failed to deactivate product. Please try again.");
      setDeactivateError(msg);
    } finally {
      setDeactivateSubmitting(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        const [prodRes, vendRes] = await Promise.all([
          getProducts(),
          getVendors().catch(() => ({ data: [] })),
        ]);

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

        const normalized = rawProds.map((p) => ({
          id: p.id,
          product_name: p.productName || p.product_name || "—",
          category: p.category ?? "—",
          description: p.description ?? "—",
          vendor_id: p.vendorId,
          vendor_name: p.vendor_name || vendorsMap[p.vendorId] || (p.vendorId ? `Vendor #${p.vendorId}` : "—"),
          unit_price: p.unitPrice ?? p.unit_price ?? 0,
          stock_quantity: p.stockQuantity ?? p.stock_quantity ?? 0,
          unit: p.unit ?? "—",
          status: p.status || "Available",
          rawProductName: p.productName || p.product_name || "",
          rawCategory: p.category || "",
          rawDescription: p.description || "",
          rawUnit: p.unit || "",
        }));

        if (!ignore) {
          setProducts(normalized);
          setVendors(rawVends);
        }
      } catch {
        if (!ignore) {
          setError("Unable to load product catalog.");
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

  const availableCount = products.filter((p) => (p.status || "Available").toLowerCase() === "available").length;
  const unavailableCount = products.filter((p) => (p.status || "").toLowerCase() === "unavailable").length;

  const statusTabs = [
    { key: "ALL", label: "All Products", count: products.length },
    { key: "AVAILABLE", label: "Available", count: availableCount },
    { key: "UNAVAILABLE", label: "Unavailable", count: unavailableCount },
  ];

  const filtered = products.filter((p) => {
    const matchesSearch =
      p.product_name.toLowerCase().includes(search.toLowerCase()) ||
      (p.category ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (p.vendor_name ?? "").toLowerCase().includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === "ALL" || (p.status || "").toUpperCase() === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedRows(new Set(filtered.map((p) => p.id)));
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
            <div className="page-title">Products</div>
            <div className="page-subtitle">
              Catalog inventory items, supplier assignments, and base unit pricing.
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} title="Refresh products">
              <RefreshCw size={13} />
              <span>Refresh</span>
            </button>
            {canManageProducts && (
              <button
                className="btn btn-primary btn-sm"
                onClick={handleOpenCreate}
                id="add-product-btn"
                title="Add a new product"
              >
                <PackagePlus size={14} />
                <span>Add Product</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {successMsg && (
        <div
          style={{
            background: "var(--success-bg)",
            border: "1px solid var(--success-border)",
            borderRadius: "var(--radius)",
            padding: "9px 12px",
            color: "var(--success)",
            fontSize: "0.8rem",
            marginBottom: 12,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
          role="status"
        >
          <span>{successMsg}</span>
          <button
            type="button"
            className="btn btn-ghost btn-sm btn-icon"
            style={{ border: "none", color: "inherit", height: "auto", padding: 0 }}
            onClick={() => setSuccessMsg("")}
            aria-label="Dismiss message"
          >
            <X size={14} />
          </button>
        </div>
      )}

      {/* Segmented Status Filter Tabs */}
      <div style={{ marginBottom: 12 }}>
        <div className="filter-tabs" role="tablist" aria-label="Product status filter">
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

      <div className="card">
        {/* Dedicated Control Toolbar (Reference C) */}
        <div className="table-toolbar">
          <div className="table-toolbar-left">
            <div className="search-box">
              <Search className="search-box-icon" size={14} />
              <input
                type="search"
                placeholder="Search products, category, vendor…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                aria-label="Search products"
              />
            </div>

            <div className="table-filter-group">
              <select
                className="table-filter-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                aria-label="Filter by status"
              >
                <option value="ALL">Status: All Statuses</option>
                <option value="AVAILABLE">Available</option>
                <option value="UNAVAILABLE">Unavailable</option>
              </select>

              <button
                type="button"
                className="table-filter-btn"
                title="Add custom filter"
              >
                <Plus size={12} />
                <span>Add filter</span>
              </button>

              {(search || statusFilter !== "ALL") && (
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  style={{ fontSize: "0.74rem", padding: "3px 8px" }}
                  onClick={() => {
                    setSearch("");
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
              Showing {filtered.length} of {products.length} products
            </span>
            <button
              className="btn btn-ghost btn-sm btn-icon"
              onClick={handleRefresh}
              title="Refresh products"
              aria-label="Refresh products"
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
          <LoadingState message="Loading products…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No products found"
            description={search || statusFilter !== "ALL" ? "No products match the current filter or search criteria." : "No product records exist yet."}
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
                        aria-label="Select all products"
                      />
                    </th>
                    <th style={{ width: 44 }}>#</th>
                    <th>Product Name</th>
                    <th>Category</th>
                    <th>Vendor</th>
                    <th style={{ textAlign: "right" }}>Unit Price</th>
                    <th>Unit</th>
                    <th>Status</th>
                    <th style={{ textAlign: "right", minWidth: 160 }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((p, i) => {
                    const isSelected = selectedRows.has(p.id);
                    return (
                      <tr key={p.id} className={isSelected ? "row-selected" : ""}>
                        <td style={{ textAlign: "center" }}>
                          <input
                            type="checkbox"
                            className="table-checkbox"
                            checked={isSelected}
                            onChange={() => handleSelectRow(p.id)}
                            aria-label={`Select product ${p.product_name}`}
                          />
                        </td>
                        <td className="table-cell-muted">{i + 1}</td>
                        <td className="table-cell-bold">{p.product_name}</td>
                        <td className="table-cell-muted">{p.category ?? "—"}</td>
                        <td>{p.vendor_name ?? "—"}</td>
                        <td className="table-num" style={{ fontWeight: 600 }}>{fmtCurrency(p.unit_price)}</td>
                        <td className="table-cell-muted">{p.unit ?? "—"}</td>
                        <td><StatusBadge status={p.status} /></td>
                        <td style={{ textAlign: "right" }}>
                          <div style={{ display: "inline-flex", gap: 6, alignItems: "center", justifyContent: "flex-end" }}>
                            {canManageProducts && (
                              <button
                                type="button"
                                className="btn btn-ghost btn-sm"
                                onClick={() => handleOpenEdit(p)}
                                title="Edit Product"
                                style={{ gap: 4, padding: "3px 8px" }}
                              >
                                <Edit size={12} /> Edit
                              </button>
                            )}
                            {canManageProducts && (p.status || "Available").toLowerCase() === "available" && (
                              <button
                                type="button"
                                className="btn btn-ghost btn-sm"
                                onClick={() => handleOpenDeactivate(p)}
                                title="Deactivate Product"
                                style={{ gap: 4, padding: "3px 8px", color: "var(--danger)" }}
                              >
                                <Ban size={12} /> Deactivate
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

      {/* ── CREATE PRODUCT MODAL ──────────────────────────────── */}
      {isCreateOpen && (
        <div
          className="modal-overlay"
          onClick={() => !createSubmitting && setIsCreateOpen(false)}
        >
          <div
            className="modal-dialog modal-dialog-lg"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-product-title"
          >
            <div className="modal-header">
              <div id="create-product-title" className="modal-title">
                Add New Product
              </div>
              <button
                type="button"
                className="btn btn-ghost btn-sm btn-icon"
                onClick={() => setIsCreateOpen(false)}
                disabled={createSubmitting}
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

                <div className="modal-section-title">Product Details</div>
                <div className="form-grid">
                  <div className="form-group form-group-full">
                    <label htmlFor="product-name-input">Product Name *</label>
                    <input
                      id="product-name-input"
                      type="text"
                      required
                      placeholder="e.g. Dell Latitude 5440"
                      value={createForm.productName}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, productName: e.target.value })
                      }
                      disabled={createSubmitting}
                      autoFocus
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="product-vendor-select">Vendor *</label>
                    <select
                      id="product-vendor-select"
                      required
                      value={createForm.vendorId}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, vendorId: e.target.value })
                      }
                      disabled={createSubmitting}
                    >
                      <option value="">Select a vendor…</option>
                      {vendors.map((v) => (
                        <option key={v.id} value={v.id}>
                          {v.vendorName || v.vendor_name || `Vendor #${v.id}`}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="product-category-input">Category</label>
                    <input
                      id="product-category-input"
                      type="text"
                      placeholder="e.g. Laptop, Monitor, Accessories"
                      value={createForm.category}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, category: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="product-price-input">Unit Price (₹) *</label>
                    <input
                      id="product-price-input"
                      type="number"
                      step="0.01"
                      min="0"
                      required
                      placeholder="e.g. 55000.00"
                      value={createForm.unitPrice}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, unitPrice: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="product-stock-input">Initial Stock Quantity</label>
                    <input
                      id="product-stock-input"
                      type="number"
                      min="0"
                      step="1"
                      placeholder="e.g. 10"
                      value={createForm.stockQuantity}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, stockQuantity: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="product-unit-input">Unit of Measure</label>
                    <input
                      id="product-unit-input"
                      type="text"
                      placeholder="e.g. Piece, Box, Set"
                      value={createForm.unit}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, unit: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="product-status-select">Status *</label>
                    <select
                      id="product-status-select"
                      value={createForm.status}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, status: e.target.value })
                      }
                      disabled={createSubmitting}
                    >
                      <option value="Available">Available</option>
                      <option value="Unavailable">Unavailable</option>
                    </select>
                  </div>

                  <div className="form-group form-group-full">
                    <label htmlFor="product-desc-input">Description</label>
                    <textarea
                      id="product-desc-input"
                      rows="2"
                      placeholder="e.g. 14 inch business laptop with 16GB RAM and 512GB SSD"
                      value={createForm.description}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, description: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => setIsCreateOpen(false)}
                  disabled={createSubmitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm"
                  disabled={createSubmitting}
                >
                  {createSubmitting ? (
                    <>
                      <RefreshCw size={13} className="spin" />
                      <span>Saving…</span>
                    </>
                  ) : (
                    <>
                      <PackagePlus size={14} />
                      <span>Create Product</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── EDIT PRODUCT MODAL ────────────────────────────────── */}
      {isEditOpen && (
        <div
          className="modal-overlay"
          onClick={() => !editSubmitting && setIsEditOpen(false)}
        >
          <div
            className="modal-dialog modal-dialog-lg"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="edit-product-title"
          >
            <div className="modal-header">
              <div id="edit-product-title" className="modal-title">
                Edit Product
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

                <div className="modal-section-title">Product Details</div>
                <div className="form-grid">
                  <div className="form-group form-group-full">
                    <label htmlFor="edit-product-name-input">Product Name *</label>
                    <input
                      id="edit-product-name-input"
                      type="text"
                      required
                      placeholder="e.g. Dell Latitude 5440"
                      value={editForm.productName}
                      onChange={(e) =>
                        setEditForm({ ...editForm, productName: e.target.value })
                      }
                      disabled={editSubmitting}
                      autoFocus
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-product-vendor-select">Vendor *</label>
                    <select
                      id="edit-product-vendor-select"
                      required
                      value={editForm.vendorId}
                      onChange={(e) =>
                        setEditForm({ ...editForm, vendorId: e.target.value })
                      }
                      disabled={editSubmitting}
                    >
                      <option value="">Select a vendor…</option>
                      {vendors.map((v) => (
                        <option key={v.id} value={v.id}>
                          {v.vendorName || v.vendor_name || `Vendor #${v.id}`}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-product-category-input">Category</label>
                    <input
                      id="edit-product-category-input"
                      type="text"
                      placeholder="e.g. Laptop, Monitor, Accessories"
                      value={editForm.category}
                      onChange={(e) =>
                        setEditForm({ ...editForm, category: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-product-price-input">Unit Price (₹) *</label>
                    <input
                      id="edit-product-price-input"
                      type="number"
                      step="0.01"
                      min="0"
                      required
                      placeholder="e.g. 55000.00"
                      value={editForm.unitPrice}
                      onChange={(e) =>
                        setEditForm({ ...editForm, unitPrice: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-product-stock-input">Stock Quantity</label>
                    <input
                      id="edit-product-stock-input"
                      type="number"
                      min="0"
                      step="1"
                      placeholder="e.g. 10"
                      value={editForm.stockQuantity}
                      onChange={(e) =>
                        setEditForm({ ...editForm, stockQuantity: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-product-unit-input">Unit of Measure</label>
                    <input
                      id="edit-product-unit-input"
                      type="text"
                      placeholder="e.g. Piece, Box, Set"
                      value={editForm.unit}
                      onChange={(e) =>
                        setEditForm({ ...editForm, unit: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-product-status-select">Status *</label>
                    <select
                      id="edit-product-status-select"
                      value={editForm.status}
                      onChange={(e) =>
                        setEditForm({ ...editForm, status: e.target.value })
                      }
                      disabled={editSubmitting}
                    >
                      <option value="Available">Available</option>
                      <option value="Unavailable">Unavailable</option>
                    </select>
                  </div>

                  <div className="form-group form-group-full">
                    <label htmlFor="edit-product-desc-input">Description</label>
                    <textarea
                      id="edit-product-desc-input"
                      rows="2"
                      placeholder="e.g. 14 inch business laptop with 16GB RAM and 512GB SSD"
                      value={editForm.description}
                      onChange={(e) =>
                        setEditForm({ ...editForm, description: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
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
                >
                  {editSubmitting ? (
                    <>
                      <RefreshCw size={13} className="spin" />
                      <span>Saving…</span>
                    </>
                  ) : (
                    <>
                      <Edit size={14} />
                      <span>Save Changes</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── DEACTIVATE PRODUCT CONFIRMATION MODAL ───────────── */}
      {productToDeactivate && (
        <div
          className="modal-overlay"
          onClick={handleCloseDeactivate}
        >
          <div
            className="modal-dialog"
            style={{ maxWidth: 480 }}
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="deactivate-product-title"
          >
            <div className="modal-header">
              <div id="deactivate-product-title" className="modal-title" style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <AlertTriangle size={16} style={{ color: "var(--danger)" }} />
                <span>Deactivate Product</span>
              </div>
              <button
                type="button"
                className="btn btn-ghost btn-sm btn-icon"
                onClick={handleCloseDeactivate}
                disabled={deactivateSubmitting}
                aria-label="Close"
              >
                <X size={15} />
              </button>
            </div>

            <div className="modal-body">
              {deactivateError && (
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
                  {deactivateError}
                </div>
              )}

              <p style={{ margin: "0 0 12px", fontSize: "0.875rem", color: "var(--text-primary)", lineHeight: 1.5 }}>
                Are you sure you want to deactivate <strong>"{productToDeactivate.product_name}"</strong>?
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
                  <li>The product status will be set to <strong>Unavailable</strong>.</li>
                  <li>The product record <strong>will not be deleted</strong> and remains in the database.</li>
                  <li>Historical orders, vendor details, and unit pricing are preserved.</li>
                </ul>
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={handleCloseDeactivate}
                disabled={deactivateSubmitting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={handleConfirmDeactivate}
                disabled={deactivateSubmitting}
                id="confirm-deactivate-btn"
              >
                {deactivateSubmitting ? (
                  <>
                    <RefreshCw size={13} className="spin" />
                    <span>Deactivating…</span>
                  </>
                ) : (
                  <>
                    <Ban size={13} />
                    <span>Deactivate Product</span>
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

export default ProductsPage;
