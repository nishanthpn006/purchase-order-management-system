import { useState, useEffect } from "react";
import { Search, PackagePlus, RefreshCw, Plus, X } from "lucide-react";
import { getProducts, getVendors, createProduct } from "../services/api";
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

function ProductsPage() {
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
            <button
              className="btn btn-primary btn-sm"
              onClick={handleOpenCreate}
              id="add-product-btn"
              title="Add a new product"
            >
              <PackagePlus size={14} />
              <span>Add Product</span>
            </button>
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
    </>
  );
}

export default ProductsPage;
