import { useState, useEffect } from "react";
import { Search, UserPlus, ExternalLink, RefreshCw, X, Plus } from "lucide-react";
import { getVendors, getVendorById } from "../services/api";
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

function VendorsPage() {
  const [vendors, setVendors]               = useState([]);
  const [loading, setLoading]               = useState(true);
  const [error, setError]                   = useState("");
  const [search, setSearch]                 = useState("");
  const [statusFilter, setStatusFilter]     = useState("ALL");
  const [selectedVendor, setSelectedVendor] = useState(null);
  const [isDetailOpen, setIsDetailOpen]     = useState(false);
  const [detailLoading, setDetailLoading]   = useState(false);
  const [refreshKey, setRefreshKey]         = useState(0);
  const [selectedRows, setSelectedRows]     = useState(new Set());

  const handleRefresh = () => {
    setLoading(true);
    setError("");
    setRefreshKey((k) => k + 1);
  };

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        const res = await getVendors();
        const raw = Array.isArray(res.data) ? res.data : (res.data?.data || []);
        const normalized = raw.map((v) => ({
          id: v.id,
          vendor_name: v.vendorName || v.vendor_name || "—",
          contact_person: v.contactPerson || v.contact_person || null,
          email: v.email || null,
          phone: v.phone || null,
          address: v.address || null,
          gst_number: v.gstNumber || v.gst_number || null,
          status: v.status || "Active",
          created_at: v.createdAt || v.created_at || null,
        }));
        if (!ignore) {
          setVendors(normalized);
        }
      } catch {
        if (!ignore) {
          setError("Unable to load vendor information.");
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

  const handleOpenDetail = async (vendorId) => {
    setIsDetailOpen(true);
    setDetailLoading(true);
    try {
      const res = await getVendorById(vendorId);
      const raw = res.data?.data || res.data;
      setSelectedVendor({
        id: raw.id,
        vendor_name: raw.vendorName || raw.vendor_name || "—",
        contact_person: raw.contactPerson || raw.contact_person || "—",
        email: raw.email || "—",
        phone: raw.phone || "—",
        address: raw.address || "—",
        gst_number: raw.gstNumber || raw.gst_number || "—",
        status: raw.status || "Active",
        created_at: raw.createdAt || raw.created_at || null,
      });
    } catch {
      const fallback = vendors.find((v) => v.id === vendorId);
      setSelectedVendor(fallback || null);
    } finally {
      setDetailLoading(false);
    }
  };

  const activeCount = vendors.filter((v) => (v.status || "Active").toLowerCase() === "active").length;
  const inactiveCount = vendors.filter((v) => (v.status || "").toLowerCase() === "inactive").length;

  const statusTabs = [
    { key: "ALL", label: "All Vendors", count: vendors.length },
    { key: "ACTIVE", label: "Active", count: activeCount },
    { key: "INACTIVE", label: "Inactive", count: inactiveCount },
  ];

  const filtered = vendors.filter((v) => {
    const matchesSearch =
      v.vendor_name.toLowerCase().includes(search.toLowerCase()) ||
      (v.contact_person ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (v.email ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (v.gst_number ?? "").toLowerCase().includes(search.toLowerCase());
    const matchesStatus =
      statusFilter === "ALL" || (v.status || "").toUpperCase() === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedRows(new Set(filtered.map((v) => v.id)));
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
            <div className="page-title">Vendors</div>
            <div className="page-subtitle">
              Supplier directory, registered tax profiles, and points of contact.
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} title="Refresh vendors">
              <RefreshCw size={13} />
              <span>Refresh</span>
            </button>
            <button className="btn btn-primary btn-sm">
              <UserPlus size={14} />
              <span>Add Vendor</span>
            </button>
          </div>
        </div>
      </div>

      {/* Segmented Status Filter Tabs */}
      <div style={{ marginBottom: 12 }}>
        <div className="filter-tabs" role="tablist" aria-label="Vendor status filter">
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
                placeholder="Search vendors, contacts, GST…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                aria-label="Search vendors"
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
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
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
              Showing {filtered.length} of {vendors.length} vendors
            </span>
            <button
              className="btn btn-ghost btn-sm btn-icon"
              onClick={handleRefresh}
              title="Refresh vendors"
              aria-label="Refresh vendors"
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
          <LoadingState message="Loading vendors…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No vendors found"
            description={search || statusFilter !== "ALL" ? "No vendors match the current filter or search criteria." : "No vendor records exist yet."}
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
                        aria-label="Select all vendors"
                      />
                    </th>
                    <th style={{ width: 44 }}>#</th>
                    <th>Vendor Name</th>
                    <th>Contact Person</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>GST Number</th>
                    <th>Status</th>
                    <th style={{ textAlign: "right" }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((v, i) => {
                    const isSelected = selectedRows.has(v.id);
                    return (
                      <tr key={v.id} className={isSelected ? "row-selected" : ""}>
                        <td style={{ textAlign: "center" }}>
                          <input
                            type="checkbox"
                            className="table-checkbox"
                            checked={isSelected}
                            onChange={() => handleSelectRow(v.id)}
                            aria-label={`Select vendor ${v.vendor_name}`}
                          />
                        </td>
                        <td className="table-cell-muted">{i + 1}</td>
                        <td className="table-cell-bold">{v.vendor_name}</td>
                        <td>{v.contact_person ?? "—"}</td>
                        <td className="table-cell-muted">{v.email ?? "—"}</td>
                        <td className="table-cell-muted">{v.phone ?? "—"}</td>
                        <td className="table-cell-mono table-cell-muted">{v.gst_number ?? "—"}</td>
                        <td><StatusBadge status={v.status} /></td>
                        <td style={{ textAlign: "right" }}>
                          <button
                            className="btn btn-ghost btn-sm"
                            onClick={() => handleOpenDetail(v.id)}
                          >
                            <ExternalLink size={12} /> View
                          </button>
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

      {/* ── VENDOR DETAILS MODAL ──────────────────────────────── */}
      {isDetailOpen && (
        <div className="modal-overlay" onClick={() => setIsDetailOpen(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="vendor-detail-title">
            <div className="modal-header">
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <div id="vendor-detail-title" className="modal-title">
                  {selectedVendor?.vendor_name || "Vendor Details"}
                </div>
                {selectedVendor && <StatusBadge status={selectedVendor.status} />}
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
                <LoadingState message="Loading vendor details…" />
              ) : selectedVendor ? (
                <div className="form-grid">
                  <div>
                    <div className="summary-tile-label">Contact Person</div>
                    <div style={{ fontWeight: 600, fontSize: "0.85rem", marginTop: 2 }}>{selectedVendor.contact_person}</div>
                  </div>
                  <div>
                    <div className="summary-tile-label">GST / Tax Number</div>
                    <div className="table-cell-mono" style={{ fontWeight: 600, fontSize: "0.85rem", marginTop: 2 }}>{selectedVendor.gst_number}</div>
                  </div>
                  <div>
                    <div className="summary-tile-label">Email</div>
                    <div style={{ fontWeight: 500, fontSize: "0.85rem", marginTop: 2 }}>{selectedVendor.email}</div>
                  </div>
                  <div>
                    <div className="summary-tile-label">Phone</div>
                    <div style={{ fontWeight: 500, fontSize: "0.85rem", marginTop: 2 }}>{selectedVendor.phone}</div>
                  </div>
                  <div className="form-group-full">
                    <div className="summary-tile-label">Registered Address</div>
                    <div style={{ fontWeight: 500, fontSize: "0.85rem", marginTop: 2 }}>{selectedVendor.address}</div>
                  </div>
                  <div className="form-group-full">
                    <div className="summary-tile-label">Registration Date</div>
                    <div style={{ fontWeight: 500, fontSize: "0.85rem", marginTop: 2 }}>{fmtDate(selectedVendor.created_at)}</div>
                  </div>
                </div>
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
    </>
  );
}

export default VendorsPage;
