import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import {
  LayoutDashboard, Users, Package, ShoppingCart,
  Boxes, ClipboardCheck, LogOut, Search,
} from "lucide-react";
import { useAuth } from "../context/useAuth";
import "../styles/poms.css";

const NAV_GROUPS = [
  {
    title: "PROCUREMENT",
    items: [
      { to: "/dashboard",       icon: LayoutDashboard, label: "Dashboard"       },
      { to: "/vendors",         icon: Users,           label: "Vendors"         },
      { to: "/products",        icon: Package,         label: "Products"        },
      { to: "/purchase-orders", icon: ShoppingCart,    label: "Purchase Orders" },
    ],
  },
  {
    title: "LOGISTICS",
    items: [
      { to: "/inventory",       icon: Boxes,           label: "Inventory"       },
      { to: "/goods-receipts",  icon: ClipboardCheck,  label: "Goods Receipts"  },
    ],
  },
];

function Sidebar({ isOpen, onClose }) {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();
  const [navSearch, setNavSearch] = useState("");

  const handleLogout = () => {
    logout();
    navigate("/", { replace: true });
  };

  const displayName = user?.name || user?.fullName || "User";
  const displayRole = role || "USER";
  const initials = (displayName
    .split(" ")
    .map((n) => n[0])
    .slice(0, 2)
    .join("")
    .toUpperCase()) || "U";

  const filteredGroups = NAV_GROUPS.map((group) => ({
    ...group,
    items: group.items.filter((item) =>
      item.label.toLowerCase().includes(navSearch.toLowerCase().trim())
    ),
  })).filter((group) => group.items.length > 0);

  return (
    <>
      {/* Mobile backdrop */}
      <div
        className={`sidebar-overlay ${isOpen ? "open" : ""}`}
        onClick={onClose}
        aria-hidden="true"
      />

      <aside className={`sidebar ${isOpen ? "open" : ""}`} role="navigation" aria-label="Main navigation">
        {/* Enterprise Brand Header */}
        <div className="sidebar-brand">
          <div className="sidebar-brand-badge" aria-hidden="true">
            P
          </div>
          <div className="sidebar-brand-meta">
            <div className="sidebar-brand-row">
              <span className="sidebar-brand-name">POMS</span>
              <span className="sidebar-brand-tag">ERP</span>
            </div>
            <span className="sidebar-brand-sub">Procurement Operations</span>
          </div>
        </div>

        {/* Compact Search Field */}
        <div className="sidebar-search-box">
          <Search size={13} className="sidebar-search-icon" aria-hidden="true" />
          <input
            type="text"
            className="sidebar-search-input"
            placeholder="Search..."
            value={navSearch}
            onChange={(e) => setNavSearch(e.target.value)}
            aria-label="Filter navigation"
          />
        </div>

        {/* Grouped Navigation */}
        <nav className="sidebar-nav">
          {filteredGroups.map((group) => (
            <div key={group.title} className="sidebar-group">
              <div className="sidebar-section-label">{group.title}</div>
              {group.items.map(({ to, icon: Icon, label }) => (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) => `sidebar-link ${isActive ? "active" : ""}`}
                  onClick={onClose}
                >
                  <Icon size={16} className="sidebar-link-icon" />
                  <span className="sidebar-link-text">{label}</span>
                </NavLink>
              ))}
            </div>
          ))}
          {filteredGroups.length === 0 && (
            <div className="sidebar-search-empty">No modules found</div>
          )}
        </nav>

        {/* User Identity & Logout Footer */}
        <div className="sidebar-footer">
          <div className="sidebar-user-preview" title={user?.email ?? ""}>
            <div className="sidebar-user-avatar">{initials}</div>
            <div className="sidebar-user-details">
              <span className="sidebar-user-name">{displayName}</span>
              <span className="sidebar-user-role">{displayRole}</span>
            </div>
          </div>
          <button className="sidebar-logout-btn" onClick={handleLogout} aria-label="Sign out">
            <LogOut size={14} />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
