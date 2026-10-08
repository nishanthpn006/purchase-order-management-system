/** Map a status string to its CSS badge class */
const STATUS_CLASS = {
  // Purchase orders
  Pending:     "badge-pending",
  Approved:    "badge-approved",
  Completed:   "badge-completed",
  Rejected:    "badge-rejected",
  Cancelled:   "badge-cancelled",
  // Vendors / products
  Active:      "badge-active",
  Inactive:    "badge-inactive",
  Available:   "badge-available",
  Unavailable: "badge-unavailable",
  // Inventory
  "In Stock":         "badge-instock",
  "Low Stock":        "badge-lowstock",
  "Reorder Required": "badge-reorder",
};

function StatusBadge({ status }) {
  if (!status) return null;
  const cls = STATUS_CLASS[status] ?? "badge-inactive";
  return (
    <span className={`badge ${cls}`}>
      <span className="badge-dot" aria-hidden="true" />
      {status}
    </span>
  );
}

export default StatusBadge;
