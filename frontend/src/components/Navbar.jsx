import { Bell, Menu, LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

const PAGE_META = {
  "/dashboard":       { title: "Dashboard",        subtitle: "Overview of procurement operations" },
  "/vendors":         { title: "Vendors",           subtitle: "Manage supplier records"            },
  "/products":        { title: "Products",          subtitle: "Product catalog and pricing"        },
  "/purchase-orders": { title: "Purchase Orders",   subtitle: "Track and manage purchase orders"  },
  "/inventory":       { title: "Inventory",         subtitle: "Monitor stock levels"               },
  "/goods-receipts":  { title: "Goods Receipts",    subtitle: "Verify and record deliveries"       },
};

function Navbar({ onMenuToggle, currentPath }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const meta = PAGE_META[currentPath] || { title: "POMS", subtitle: "" };

  const initials = user?.name
    ? user.name.split(" ").map((n) => n[0]).slice(0, 2).join("").toUpperCase()
    : "U";

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  return (
    <header className="navbar">
      <div className="navbar-left">
        <button
          className="hamburger"
          onClick={onMenuToggle}
          aria-label="Toggle navigation"
        >
          <Menu size={20} />
        </button>

        <div>
          <div className="navbar-title">{meta.title}</div>
          <div className="navbar-subtitle">{meta.subtitle}</div>
        </div>
      </div>

      <div className="navbar-right">
        {/* Notification bell */}
        <button className="navbar-icon-btn" aria-label="Notifications" title="Notifications">
          <Bell size={16} />
          <span className="notification-dot" aria-hidden="true" />
        </button>

        <div className="navbar-divider" />

        {/* User profile */}
        <div className="user-menu" title={user?.email}>
          <div className="user-avatar">{initials}</div>
          <div className="user-info">
            <span className="user-name">{user?.name ?? "User"}</span>
            <span className="user-role">{user?.role ?? ""}</span>
          </div>
        </div>

        {/* Logout */}
        <button
          className="navbar-icon-btn"
          onClick={handleLogout}
          aria-label="Logout"
          title="Logout"
        >
          <LogOut size={16} />
        </button>
      </div>
    </header>
  );
}

export default Navbar;
