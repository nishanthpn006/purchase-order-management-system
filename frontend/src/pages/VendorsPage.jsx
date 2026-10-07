import { useState, useEffect } from "react";
import { Search, UserPlus, ExternalLink, RefreshCw, X, Plus, Edit } from "lucide-react";
import { getVendors, getVendorById, createVendor, updateVendor } from "../services/api";
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

const initialCreateForm = {
  vendorName: "",
  contactPerson: "",
  email: "",
  phone: "",
  address: "",
  gstNumber: "",
  status: "Active",
};

const initialEditForm = {
  id: null,
  vendorName: "",
  contactPerson: "",
  email: "",
  phone: "",
  address: "",
  gstNumber: "",
  status: "Active",
};

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

  // Create Vendor modal state
  const [isCreateOpen, setIsCreateOpen]         = useState(false);
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createError, setCreateError]           = useState("");
  const [successMsg, setSuccessMsg]             = useState("");
  const [createForm, setCreateForm]             = useState(initialCreateForm);

  // Edit Vendor modal state
  const [isEditOpen, setIsEditOpen]             = useState(false);
  const [editSubmitting, setEditSubmitting]     = useState(false);
  const [editError, setEditError]               = useState("");
  const [editForm, setEditForm]                 = useState(initialEditForm);

  const handleOpenCreate = () => {
    setCreateForm(initialCreateForm);
    setCreateError("");
    setIsCreateOpen(true);
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreateError("");

    const trimmedName = createForm.vendorName?.trim();
    if (!trimmedName) {
      setCreateError("Vendor name is required.");
      return;
    }

    if (createForm.email && createForm.email.trim()) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(createForm.email.trim())) {
        setCreateError("Please enter a valid email address.");
        return;
      }
    }

    setCreateSubmitting(true);
    try {
      const payload = {
        vendorName: trimmedName,
        contactPerson: createForm.contactPerson?.trim() || null,
        email: createForm.email?.trim() || null,
        phone: createForm.phone?.trim() || null,
        address: createForm.address?.trim() || null,
        gstNumber: createForm.gstNumber?.trim() || null,
        status: createForm.status || "Active",
      };

      const res = await createVendor(payload);
      const created = res.data;
      setSuccessMsg(`Vendor "${created.vendorName || created.vendor_name || trimmedName}" created successfully.`);
      setIsCreateOpen(false);
      setCreateForm(initialCreateForm);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Access denied: Only Admin and Manager roles can add vendors."
          : "Failed to create vendor. Please check inputs.");
      setCreateError(msg);
    } finally {
      setCreateSubmitting(false);
    }
  };

  const handleOpenEdit = (v) => {
    setEditForm({
      id: v.id,
      vendorName: (v.rawVendorName ?? (v.vendor_name !== "—" ? v.vendor_name : "")) || "",
      contactPerson: (v.rawContactPerson ?? (v.contact_person !== "—" ? v.contact_person : "")) || "",
      email: (v.rawEmail ?? (v.email !== "—" ? v.email : "")) || "",
      phone: (v.rawPhone ?? (v.phone !== "—" ? v.phone : "")) || "",
      address: (v.rawAddress ?? (v.address !== "—" ? v.address : "")) || "",
      gstNumber: (v.rawGstNumber ?? (v.gst_number !== "—" ? v.gst_number : "")) || "",
      status: v.status || "Active",
    });
    setEditError("");
    setIsEditOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (editSubmitting) return;
    setEditError("");

    const trimmedName = editForm.vendorName?.trim();
    if (!trimmedName) {
      setEditError("Vendor name is required.");
      return;
    }

    if (editForm.email && editForm.email.trim()) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(editForm.email.trim())) {
        setEditError("Please enter a valid email address.");
        return;
      }
    }

    setEditSubmitting(true);
    try {
      const payload = {
        vendorName: trimmedName,
        contactPerson: editForm.contactPerson?.trim() || null,
        email: editForm.email?.trim() || null,
        phone: editForm.phone?.trim() || null,
        address: editForm.address?.trim() || null,
        gstNumber: editForm.gstNumber?.trim() || null,
        status: editForm.status || "Active",
      };

      const res = await updateVendor(editForm.id, payload);
      const updated = res.data;
      const updatedName = updated.vendorName || updated.vendor_name || trimmedName;
      setSuccessMsg(`Vendor "${updatedName}" updated successfully.`);
      setIsEditOpen(false);

      // Immediately display updated values
      setVendors((prev) =>
        prev.map((v) =>
          v.id === editForm.id
            ? {
                ...v,
                vendor_name: updatedName,
                rawVendorName: updatedName,
                contact_person: updated.contactPerson || updated.contact_person || payload.contactPerson,
                rawContactPerson: updated.contactPerson || updated.contact_person || (payload.contactPerson || ""),
                email: updated.email || payload.email,
                rawEmail: updated.email || (payload.email || ""),
                phone: updated.phone || payload.phone,
                rawPhone: updated.phone || (payload.phone || ""),
                address: updated.address || payload.address,
                rawAddress: updated.address || (payload.address || ""),
                gst_number: updated.gstNumber || updated.gst_number || payload.gstNumber,
                rawGstNumber: updated.gstNumber || updated.gst_number || (payload.gstNumber || ""),
                status: updated.status || payload.status,
              }
            : v
        )
      );

      // Refresh the vendor list from API
      setRefreshKey((k) => k + 1);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        (err.response?.status === 403
          ? "Access denied: Only Admin and Manager roles can update vendors."
          : err.response?.status === 404
          ? "Vendor not found."
          : "Failed to update vendor. Please check inputs.");
      setEditError(msg);
    } finally {
      setEditSubmitting(false);
    }
  };

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
          rawVendorName: v.vendorName || v.vendor_name || "",
          contact_person: v.contactPerson || v.contact_person || null,
          rawContactPerson: v.contactPerson || v.contact_person || "",
          email: v.email || null,
          rawEmail: v.email || "",
          phone: v.phone || null,
          rawPhone: v.phone || "",
          address: v.address || null,
          rawAddress: v.address || "",
          gst_number: v.gstNumber || v.gst_number || null,
          rawGstNumber: v.gstNumber || v.gst_number || "",
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
            <button
              className="btn btn-primary btn-sm"
              onClick={handleOpenCreate}
              id="add-vendor-btn"
              title="Add a new supplier vendor"
            >
              <UserPlus size={14} />
              <span>Add Vendor</span>
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
                          <div style={{ display: "inline-flex", gap: 6, alignItems: "center", justifyContent: "flex-end" }}>
                            <button
                              type="button"
                              className="btn btn-ghost btn-sm"
                              onClick={() => handleOpenDetail(v.id)}
                              title="View Vendor Details"
                              style={{ gap: 4, padding: "3px 8px" }}
                            >
                              <ExternalLink size={12} /> View
                            </button>
                            <button
                              type="button"
                              className="btn btn-ghost btn-sm"
                              onClick={() => handleOpenEdit(v)}
                              title="Edit Vendor"
                              id={`edit-vendor-${v.id}-btn`}
                              style={{ gap: 4, padding: "3px 8px" }}
                            >
                              <Edit size={12} /> Edit
                            </button>
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
              {selectedVendor && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => {
                    const vendorObj = vendors.find((v) => v.id === selectedVendor.id) || selectedVendor;
                    setIsDetailOpen(false);
                    handleOpenEdit(vendorObj);
                  }}
                  style={{ gap: 4 }}
                >
                  <Edit size={13} />
                  <span>Edit</span>
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ── CREATE VENDOR MODAL ──────────────────────────────── */}
      {isCreateOpen && (
        <div
          className="modal-overlay"
          onClick={() => !createSubmitting && setIsCreateOpen(false)}
        >
          <div
            className="modal-dialog"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-vendor-title"
          >
            <div className="modal-header">
              <div id="create-vendor-title" className="modal-title">
                Add New Vendor
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

                <div className="modal-section-title">Vendor Information</div>
                <div className="form-grid">
                  <div className="form-group form-group-full">
                    <label htmlFor="vendor-name-input">Vendor Name *</label>
                    <input
                      id="vendor-name-input"
                      type="text"
                      required
                      placeholder="e.g. Acme Supplies Ltd"
                      value={createForm.vendorName}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, vendorName: e.target.value })
                      }
                      disabled={createSubmitting}
                      autoFocus
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="vendor-contact-input">Contact Person</label>
                    <input
                      id="vendor-contact-input"
                      type="text"
                      placeholder="e.g. Alice Smith"
                      value={createForm.contactPerson}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, contactPerson: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="vendor-email-input">Email</label>
                    <input
                      id="vendor-email-input"
                      type="email"
                      placeholder="e.g. contact@acme.com"
                      value={createForm.email}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, email: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="vendor-phone-input">Phone</label>
                    <input
                      id="vendor-phone-input"
                      type="tel"
                      placeholder="e.g. 9876543210"
                      value={createForm.phone}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, phone: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="vendor-gst-input">GST Number</label>
                    <input
                      id="vendor-gst-input"
                      type="text"
                      placeholder="e.g. 29ABCDE1234F1Z5"
                      value={createForm.gstNumber}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, gstNumber: e.target.value })
                      }
                      disabled={createSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="vendor-status-select">Status *</label>
                    <select
                      id="vendor-status-select"
                      value={createForm.status}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, status: e.target.value })
                      }
                      disabled={createSubmitting}
                    >
                      <option value="Active">Active</option>
                      <option value="Inactive">Inactive</option>
                    </select>
                  </div>

                  <div className="form-group form-group-full">
                    <label htmlFor="vendor-address-input">Registered Address</label>
                    <textarea
                      id="vendor-address-input"
                      rows="2"
                      placeholder="e.g. 123 Industrial Way, Tech Park, Bangalore"
                      value={createForm.address}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, address: e.target.value })
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
                      <UserPlus size={14} />
                      <span>Create Vendor</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── EDIT VENDOR MODAL ────────────────────────────────── */}
      {isEditOpen && (
        <div
          className="modal-overlay"
          onClick={() => !editSubmitting && setIsEditOpen(false)}
        >
          <div
            className="modal-dialog"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="edit-vendor-title"
          >
            <div className="modal-header">
              <div id="edit-vendor-title" className="modal-title">
                Edit Vendor
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

                <div className="modal-section-title">Vendor Information</div>
                <div className="form-grid">
                  <div className="form-group form-group-full">
                    <label htmlFor="edit-vendor-name-input">Vendor Name *</label>
                    <input
                      id="edit-vendor-name-input"
                      type="text"
                      required
                      placeholder="e.g. Acme Supplies Ltd"
                      value={editForm.vendorName}
                      onChange={(e) =>
                        setEditForm({ ...editForm, vendorName: e.target.value })
                      }
                      disabled={editSubmitting}
                      autoFocus
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-vendor-contact-input">Contact Person</label>
                    <input
                      id="edit-vendor-contact-input"
                      type="text"
                      placeholder="e.g. Alice Smith"
                      value={editForm.contactPerson}
                      onChange={(e) =>
                        setEditForm({ ...editForm, contactPerson: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-vendor-email-input">Email</label>
                    <input
                      id="edit-vendor-email-input"
                      type="email"
                      placeholder="e.g. contact@acme.com"
                      value={editForm.email}
                      onChange={(e) =>
                        setEditForm({ ...editForm, email: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-vendor-phone-input">Phone</label>
                    <input
                      id="edit-vendor-phone-input"
                      type="tel"
                      placeholder="e.g. 9876543210"
                      value={editForm.phone}
                      onChange={(e) =>
                        setEditForm({ ...editForm, phone: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-vendor-gst-input">GST Number</label>
                    <input
                      id="edit-vendor-gst-input"
                      type="text"
                      placeholder="e.g. 29ABCDE1234F1Z5"
                      value={editForm.gstNumber}
                      onChange={(e) =>
                        setEditForm({ ...editForm, gstNumber: e.target.value })
                      }
                      disabled={editSubmitting}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="edit-vendor-status-select">Status *</label>
                    <select
                      id="edit-vendor-status-select"
                      value={editForm.status}
                      onChange={(e) =>
                        setEditForm({ ...editForm, status: e.target.value })
                      }
                      disabled={editSubmitting}
                    >
                      <option value="Active">Active</option>
                      <option value="Inactive">Inactive</option>
                    </select>
                  </div>

                  <div className="form-group form-group-full">
                    <label htmlFor="edit-vendor-address-input">Registered Address</label>
                    <textarea
                      id="edit-vendor-address-input"
                      rows="2"
                      placeholder="e.g. 123 Industrial Way, Tech Park, Bangalore"
                      value={editForm.address}
                      onChange={(e) =>
                        setEditForm({ ...editForm, address: e.target.value })
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
    </>
  );
}

export default VendorsPage;
