import { useState, useEffect } from "react";
import { Search, PackagePlus, RefreshCw } from "lucide-react";
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
  const [products, setProducts] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");
  const [search, setSearch]     = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

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
          setError("Unable to load product information.");
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
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} title="Refresh">
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
            <button className="btn btn-ghost btn-sm" onClick={handleRefresh} style={{ marginTop: 8 }}>
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
