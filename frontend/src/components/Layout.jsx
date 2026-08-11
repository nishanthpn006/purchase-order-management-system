import { useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";
import "../styles/poms.css";

/**
 * Layout — persistent shell for all protected pages.
 * Sidebar stays fixed on the left; Navbar sticks to top.
 * Content renders via <Outlet />.
 */
function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  return (
    <div className="poms-layout">
      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      <div className="poms-main">
        <Navbar
          onMenuToggle={() => setSidebarOpen((prev) => !prev)}
          currentPath={location.pathname}
        />

        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default Layout;
