import { Bell, Menu, LogOut, Search } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

const PAGE_META = {
  "/dashboard":       { title: "Dashboard",        subtitle: "Procurement operations & KPI summary" },
  "/vendors":         { title: "Vendors",           subtitle: "Supplier directory & contact records" },
  "/products":        { title: "Products",          subtitle: "Catalog items, pricing & availability" },
  "/purchase-orders": { title: "Purchase Orders",   subtitle: "Order lifecycle tracking & approvals" },
  "/inventory":       { title: "Inventory",         subtitle: "Stock monitoring & replenishment"      },
  "/goods-receipts":  { title: "Goods Receipts",    subtitle: "Delivery confirmations & receipts"    },
};

function Navbar({ onMenuToggle, currentPath }) {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();

  const meta = PAGE_META[currentPath] || { title: "POMS", subtitle: "Enterprise Procurement" };

  const displayName = user?.name || user?.fullName || "User";
  const displayRole = role || "USER";
  const initials = (displayName
    .split(" ")
    .map((n) => n[0])
    .slice(0, 2)
    .join("")
    .toUpperCase()) || "U";

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  return (
    <header className="navbar" role="banner">
      <div className="navbar-left">
        <button
          className="hamburger"
          onClick={onMenuToggle}
          aria-label="Toggle navigation menu"
        >
          <Menu size={18} />
        </button>

        <div>
          <div className="navbar-title">{meta.title}</div>
          <div className="navbar-subtitle">{meta.subtitle}</div>
        </div>
      </div>

      {/* Center Enterprise Quick Search (Reference C) */}
      <div className="navbar-center-search" role="search">
        <Search size={13} className="navbar-center-search-icon" aria-hidden="true" />
        <input
          type="search"
          placeholder="Search orders, vendors, catalog items..."
          className="navbar-center-search-input"
          aria-label="Global search"
        />
      </div>

      <div className="navbar-right">
        {/* System Health / Status Indicator */}
        <div className="system-status-indicator" title="System operational and connected">
          <span className="system-status-dot" aria-hidden="true" />
          <span>Connected</span>
        </div>

        {/* Notifications */}
        <button
          className="navbar-icon-btn"
          aria-label="Notifications"
          title="System notifications"
        >
          <Bell size={15} />
          <span className="notification-dot" aria-hidden="true" />
        </button>

        <div className="navbar-divider" aria-hidden="true" />

        {/* User profile with role badge */}
        <div className="user-menu" title={`Signed in as ${user?.email ?? ""}`}>
          <div className="user-avatar">{initials}</div>
          <div className="user-info">
            <span className="user-name">{displayName}</span>
            <span className="role-tag">{displayRole}</span>
          </div>
        </div>

        {/* Sign out */}
        <button
          className="navbar-icon-btn"
          onClick={handleLogout}
          aria-label="Sign out"
          title="Sign out"
        >
          <LogOut size={15} />
        </button>
      </div>
    </header>
  );
}

export default Navbar;
