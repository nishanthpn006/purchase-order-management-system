import { useState, useEffect } from "react";
import { Search, UserPlus, ExternalLink, RefreshCw } from "lucide-react";
import { getVendors } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

function VendorsPage() {
  const [vendors, setVendors]   = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");
  const [search, setSearch]     = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await getVendors();
      setVendors(res.data.data);
    } catch {
      setError("Unable to load vendor information.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

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
            <button className="btn btn-ghost btn-sm" onClick={load} title="Refresh">
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
            <button className="btn btn-ghost btn-sm" onClick={load} style={{ marginTop: 8 }}>
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
                      <button className="btn btn-ghost btn-sm">
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
    </>
  );
}

export default VendorsPage;
