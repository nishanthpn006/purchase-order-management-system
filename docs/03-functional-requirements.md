# Functional Requirements

## Introduction

The Purchase Order Management System (POMS) is designed to automate and streamline the procurement process within an organization. The system enables employees to submit purchase requests, allows managers to review and approve requests, manages vendors, generates purchase orders, and tracks the entire purchasing lifecycle.

---

# Functional Requirements

## FR-01: User Authentication

- The system shall allow users to register with valid credentials.
- The system shall allow registered users to log in securely.
- The system shall encrypt user passwords before storing them.
- The system shall support role-based authentication.
- The system shall allow users to log out securely.

---

## FR-02: User Management

- The administrator shall create, update, and delete user accounts.
- The administrator shall assign roles to users.
- The system shall maintain user profile information.
- The system shall allow administrators to activate or deactivate user accounts.

---

## FR-03: Purchase Request Management

- Employees shall be able to create purchase requests.
- Employees shall provide item name, quantity, estimated price, justification, and required date.
- The system shall generate a unique request ID.
- Employees shall edit requests before approval.
- Employees shall cancel pending requests.

---

## FR-04: Approval Workflow

- Managers shall review submitted purchase requests.
- Managers shall approve or reject requests.
- Managers shall provide comments when rejecting requests.
- The system shall notify employees after approval or rejection.
- Requests shall move through predefined approval stages.

---

## FR-05: Vendor Management

- The administrator shall add vendor details.
- The administrator shall update vendor information.
- The administrator shall remove inactive vendors.
- The system shall store vendor contact details.
- The system shall maintain vendor history.

---

## FR-06: Purchase Order Generation

- Approved purchase requests shall be converted into purchase orders.
- The system shall generate unique purchase order numbers.
- Purchase orders shall include vendor information, item details, pricing, and approval details.
- Purchase orders shall be downloadable in PDF format.

---

## FR-07: Inventory Verification

- The system shall verify stock availability before generating purchase requests.
- The system shall prevent duplicate purchase requests for available inventory.
- The system shall notify users if sufficient stock exists.

---

## FR-08: Notifications

- The system shall notify managers about pending approvals.
- The system shall notify employees about request status changes.
- The system shall send notifications for purchase order creation.
- The system shall notify vendors when purchase orders are issued (future enhancement).

---

## FR-09: Reports

- The administrator shall generate monthly purchase reports.
- The administrator shall generate vendor-wise reports.
- The administrator shall generate department-wise purchase reports.
- Reports shall be exportable as PDF or Excel files.

---

## FR-10: Search and Filter

- Users shall search purchase requests by request ID.
- Users shall search purchase orders by PO number.
- Users shall filter requests by status.
- Users shall filter records by department, vendor, and date.

---

## FR-11: Audit Trail

- The system shall record every important action.
- The system shall store timestamps for all activities.
- The system shall record the user responsible for each activity.
- Audit records shall not be editable.

---

## FR-12: Dashboard

- Employees shall view their submitted requests.
- Managers shall view pending approvals.
- Administrators shall view system statistics.
- The dashboard shall display recent purchase activities.

---

# Functional Summary

The Purchase Order Management System shall provide:

- Secure user authentication.
- Role-based access control.
- Purchase request creation.
- Approval workflow.
- Vendor management.
- Purchase order generation.
- Inventory verification.
- Notifications.
- Reporting.
- Search and filtering.
- Audit trail.
- Dashboard with analytics.