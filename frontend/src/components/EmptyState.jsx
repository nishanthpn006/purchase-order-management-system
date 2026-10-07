import { Inbox } from "lucide-react";

function EmptyState({ title = "No records found", description = "There are no records to display.", action }) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon" aria-hidden="true">
        <Inbox size={20} />
      </div>
      <p className="empty-state-title">{title}</p>
      <p className="empty-state-desc">{description}</p>
      {action && <div style={{ marginTop: 10 }}>{action}</div>}
    </div>
  );
}

export default EmptyState;
