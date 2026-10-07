import { useState, useEffect } from "react";
import { Search, RefreshCw, Plus } from "lucide-react";
import { getInventory, getProducts, getVendors } from "../services/api";
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
  const [inventory, setInventory]   = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState("");
  const [search, setSearch]         = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
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
        const [invRes, prodRes, vendRes] = await Promise.all([
          getInventory(),
          getProducts().catch(() => ({ data: [] })),
          getVendors().catch(() => ({ data: [] })),
        ]);

        const rawInv = Array.isArray(invRes.data)
          ? invRes.data
          : (invRes.data?.data || []);
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

        const productsMap = {};
        rawProds.forEach((p) => {
          productsMap[p.id] = {
            name: p.productName || p.product_name,
            category: p.category,
            vendorName: p.vendor_name || vendorsMap[p.vendorId],
          };
        });

        const normalized = rawInv.map((item) => {
          const prodInfo = productsMap[item.productId] || {};
          return {
            id: item.id,
            product_id: item.productId,
            product_name: item.product_name || prodInfo.name || `Product #${item.productId || "—"}`,
            category: prodInfo.category ?? "—",
            vendor_name: prodInfo.vendorName ?? "—",
            quantity_in_stock: item.quantityInStock ?? item.quantity_in_stock ?? 0,
            reorder_level: item.reorderLevel ?? item.reorder_level ?? 0,
            last_updated: item.lastUpdated || item.last_updated,
          };
        });

        if (!ignore) {
          setInventory(normalized);
        }
      } catch {
        if (!ignore) {
          setError("Unable to load inventory information.");
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

  const inStockCount = inventory.filter((i) => getStockStatus(i.quantity_in_stock, i.reorder_level) === "In Stock").length;
  const lowStockCount = inventory.filter((i) => getStockStatus(i.quantity_in_stock, i.reorder_level) === "Low Stock").length;
  const reorderCount = inventory.filter((i) => getStockStatus(i.quantity_in_stock, i.reorder_level) === "Reorder Required").length;

  const statusTabs = [
    { key: "ALL", label: "All Stock", count: inventory.length },
    { key: "IN STOCK", label: "In Stock", count: inStockCount },
    { key: "LOW STOCK", label: "Low Stock", count: lowStockCount },
    { key: "REORDER REQUIRED", label: "Reorder", count: reorderCount },
  ];

  const filtered = inventory.filter((item) => {
    const itemStatus = getStockStatus(item.quantity_in_stock, item.reorder_level).toUpperCase();
    const matchesSearch =
      item.product_name.toLowerCase().includes(search.toLowerCase()) ||
      (item.category ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (item.vendor_name ?? "").toLowerCase().includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === "ALL" || itemStatus === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedRows(new Set(filtered.map((item) => item.id)));
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
            <div className="page-title">Inventory</div>
            <div className="page-subtitle">
              Warehouse stock monitoring, automated reorder thresholds, and inventory allocation.
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} title="Refresh inventory">
              <RefreshCw size={13} />
              <span>Refresh</span>
            </button>
          </div>
        </div>
      </div>

      {/* Segmented Status Filter Tabs */}
      <div style={{ marginBottom: 12 }}>
        <div className="filter-tabs" role="tablist" aria-label="Stock status filter">
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
                aria-label="Search inventory"
              />
            </div>

            <div className="table-filter-group">
              <select
                className="table-filter-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                aria-label="Filter by stock status"
              >
                <option value="ALL">Status: All Stock</option>
                <option value="IN STOCK">In Stock</option>
                <option value="LOW STOCK">Low Stock</option>
                <option value="REORDER REQUIRED">Reorder Required</option>
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
              Showing {filtered.length} of {inventory.length} items
            </span>
            <button
              className="btn btn-ghost btn-sm btn-icon"
              onClick={handleRefresh}
              title="Refresh inventory"
              aria-label="Refresh inventory"
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
          <LoadingState message="Loading inventory…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No inventory records found"
            description={search || statusFilter !== "ALL" ? "No items match the current filter or search criteria." : "Inventory records will appear here."}
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
                        aria-label="Select all inventory items"
                      />
                    </th>
                    <th style={{ width: 44 }}>#</th>
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
                    const isSelected = selectedRows.has(item.id);
                    return (
                      <tr key={item.id} className={isSelected ? "row-selected" : ""}>
                        <td style={{ textAlign: "center" }}>
                          <input
                            type="checkbox"
                            className="table-checkbox"
                            checked={isSelected}
                            onChange={() => handleSelectRow(item.id)}
                            aria-label={`Select item ${item.product_name}`}
                          />
                        </td>
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

export default InventoryPage;
