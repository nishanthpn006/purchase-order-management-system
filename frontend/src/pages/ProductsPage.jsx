import { useState, useEffect } from "react";
import { Search, PackagePlus, RefreshCw, Plus } from "lucide-react";
import { getProducts, getVendors } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

function fmtCurrency(amount) {
  if (amount == null) return "—";
  return "₹" + Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2 });
}

function ProductsPage() {
  const [products, setProducts]       = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState("");
  const [search, setSearch]           = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [refreshKey, setRefreshKey]   = useState(0);
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
            <button className="btn btn-primary btn-sm">
              <PackagePlus size={14} />
              <span>Add Product</span>
            </button>
          </div>
        </div>
      </div>

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
    </>
  );
}

export default ProductsPage;
