import { useState, useEffect } from "react";
import { Search, PackagePlus, RefreshCw } from "lucide-react";
import { getProducts } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import "../styles/poms.css";

function fmtCurrency(amount) {
  if (amount == null) return "—";
  return "₹" + Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2 });
}

function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");
  const [search, setSearch]     = useState("");

  const load = async () => {
    setLoading(true); setError("");
    try {
      const res = await getProducts();
      setProducts(res.data.data);
    } catch {
      setError("Unable to load product information.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const filtered = products.filter(
    (p) =>
      p.product_name.toLowerCase().includes(search.toLowerCase()) ||
      (p.category ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (p.vendor_name ?? "").toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div className="page-title">Products</div>
            <div className="page-subtitle">
              {!loading && `${products.length} product${products.length !== 1 ? "s" : ""} in catalog`}
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <div className="search-box">
              <Search className="search-box-icon" size={16} />
              <input
                type="search"
                placeholder="Search products…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button className="btn btn-ghost btn-sm" onClick={load} title="Refresh">
              <RefreshCw size={14} />
            </button>
            <button className="btn btn-primary btn-sm">
              <PackagePlus size={14} />
              Add Product
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
          <LoadingState message="Loading products…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No products found"
            description={search ? `No products match "${search}".` : "No product records exist yet."}
          />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Product Name</th>
                  <th>Category</th>
                  <th>Vendor</th>
                  <th>Unit Price</th>
                  <th>Unit</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((p, i) => (
                  <tr key={p.id}>
                    <td className="table-cell-muted">{i + 1}</td>
                    <td className="table-cell-bold">{p.product_name}</td>
                    <td className="table-cell-muted">{p.category ?? "—"}</td>
                    <td>{p.vendor_name ?? "—"}</td>
                    <td style={{ fontWeight: 600 }}>{fmtCurrency(p.unit_price)}</td>
                    <td className="table-cell-muted">{p.unit ?? "—"}</td>
                    <td><StatusBadge status={p.status} /></td>
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

export default ProductsPage;
