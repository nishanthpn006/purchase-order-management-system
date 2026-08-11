import { NavLink, useNavigate } from "react-router-dom";
import {
  LayoutDashboard, Users, Package, ShoppingCart,
  Boxes, ClipboardCheck, LogOut,
} from "lucide-react";
import { useAuth } from "../context/useAuth";
import "../styles/poms.css";

const NAV_ITEMS = [
  { to: "/dashboard",       icon: LayoutDashboard, label: "Dashboard"       },
  { to: "/vendors",         icon: Users,           label: "Vendors"         },
  { to: "/products",        icon: Package,         label: "Products"        },
  { to: "/purchase-orders", icon: ShoppingCart,    label: "Purchase Orders" },
  { to: "/inventory",       icon: Boxes,           label: "Inventory"       },
  { to: "/goods-receipts",  icon: ClipboardCheck,  label: "Goods Receipts"  },
];

function Sidebar({ isOpen, onClose }) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  return (
    <>
      {/* Mobile overlay */}
      <div
        className={`sidebar-overlay ${isOpen ? "open" : ""}`}
        onClick={onClose}
        aria-hidden="true"
      />

      <aside className={`sidebar ${isOpen ? "open" : ""}`} role="navigation" aria-label="Main navigation">
        {/* Brand */}
        <div className="sidebar-brand">
          <div className="sidebar-brand-name">POMS</div>
          <div className="sidebar-brand-sub">Purchase Order Management System</div>
        </div>

        {/* Navigation */}
        <nav className="sidebar-nav">
          <div className="sidebar-section-label">Main Menu</div>

          {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `sidebar-link ${isActive ? "active" : ""}`}
              onClick={onClose}
            >
              <Icon size={17} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        {/* Logout footer */}
        <div className="sidebar-footer">
          <button className="sidebar-link" onClick={handleLogout} style={{ width: "100%", color: "#f87171" }}>
            <LogOut size={17} />
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
