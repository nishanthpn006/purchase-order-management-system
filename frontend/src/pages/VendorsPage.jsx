import { useState, useEffect } from "react";
import { Search, UserPlus, ExternalLink, RefreshCw, X } from "lucide-react";
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
  const [selectedVendor, setSelectedVendor] = useState(null);
  const [isDetailOpen, setIsDetailOpen]     = useState(false);
  const [detailLoading, setDetailLoading]   = useState(false);

  const load = async () => {
    setLoading(true);
    setError("");
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
      setVendors(normalized);
    } catch {
      setError("Unable to load vendor information.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

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

  const filtered = vendors.filter(
    (v) =>
      v.vendor_name.toLowerCase().includes(search.toLowerCase()) ||
      (v.contact_person ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (v.email ?? "").toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Vendors</div>
            <div className="page-subtitle">
              {!loading && `${vendors.length} supplier${vendors.length !== 1 ? "s" : ""} registered`}
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <div className="search-box">
              <Search className="search-box-icon" size={16} />
              <input
                type="search"
                placeholder="Search vendors…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button className="btn btn-ghost btn-sm" onClick={() => { setLoading(true); load(); }} title="Refresh">
              <RefreshCw size={14} />
            </button>
            <button className="btn btn-primary btn-sm">
              <UserPlus size={14} />
              Add Vendor
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        {error ? (
          <div className="empty-state">
            <p style={{ color: "var(--danger)", fontSize: "0.88rem" }}>{error}</p>
            <button className="btn btn-ghost btn-sm" onClick={() => { setLoading(true); load(); }} style={{ marginTop: 8 }}>
              <RefreshCw size={13} /> Retry
            </button>
          </div>
        ) : loading ? (
          <LoadingState message="Loading vendors…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No vendors found"
            description={search ? `No vendors match "${search}".` : "No vendor records exist yet."}
          />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Vendor Name</th>
                  <th>Contact Person</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>GST Number</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((v, i) => (
                  <tr key={v.id}>
                    <td className="table-cell-muted">{i + 1}</td>
                    <td className="table-cell-bold">{v.vendor_name}</td>
                    <td>{v.contact_person ?? "—"}</td>
                    <td className="table-cell-muted">{v.email ?? "—"}</td>
                    <td className="table-cell-muted">{v.phone ?? "—"}</td>
                    <td className="table-cell-mono table-cell-muted">{v.gst_number ?? "—"}</td>
                    <td><StatusBadge status={v.status} /></td>
                    <td>
                      <button
                        className="btn btn-ghost btn-sm"
                        onClick={() => handleOpenDetail(v.id)}
                      >
                        <ExternalLink size={13} /> View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── VENDOR DETAILS MODAL ──────────────────────────────── */}
      {isDetailOpen && (
        <div className="modal-overlay" onClick={() => setIsDetailOpen(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <div className="modal-title">
                  {selectedVendor?.vendor_name || "Vendor Details"}
                </div>
                {selectedVendor && (
                  <div style={{ marginTop: 4 }}>
                    <StatusBadge status={selectedVendor.status} />
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
                <LoadingState message="Loading vendor details…" />
              ) : selectedVendor ? (
                <div className="form-grid">
                  <div>
                    <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Contact Person</div>
                    <div style={{ fontWeight: 600 }}>{selectedVendor.contact_person}</div>
                  </div>
                  <div>
                    <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>GST Number</div>
                    <div className="table-cell-mono" style={{ fontWeight: 600 }}>{selectedVendor.gst_number}</div>
                  </div>
                  <div>
                    <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Email</div>
                    <div style={{ fontWeight: 600 }}>{selectedVendor.email}</div>
                  </div>
                  <div>
                    <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Phone</div>
                    <div style={{ fontWeight: 600 }}>{selectedVendor.phone}</div>
                  </div>
                  <div className="form-group-full">
                    <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Address</div>
                    <div style={{ fontWeight: 600 }}>{selectedVendor.address}</div>
                  </div>
                  <div className="form-group-full">
                    <div className="table-cell-muted" style={{ fontSize: "0.75rem" }}>Registered Date</div>
                    <div style={{ fontWeight: 600 }}>{fmtDate(selectedVendor.created_at)}</div>
                  </div>
                </div>
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

export default VendorsPage;
