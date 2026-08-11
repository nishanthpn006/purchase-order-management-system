# User Roles

## 1. Introduction

The Purchase Order Management System (POMS) database schema supports role distinction through the `role` column in the `users` table, defined as `ENUM('Admin', 'Manager', 'Employee')`.

This document reflects the **Current Implementation Status (Review-I)** and outlines the **Planned Role-Based Access Control (RBAC) Roadmap**.

---

## 2. Actual Database Roles (`users.role`)

### 1. Administrator (`Admin`) — CURRENTLY IMPLEMENTED

- **Database Enum**: `'Admin'`
- **Current Status**: Active and implemented.
- **Current Authorization**: Full access to the POMS application shell, operations dashboard, vendor list, product catalog, purchase orders overview, inventory tracking, and goods receipts.
- **Demo Account**: `admin@poms.com`

### 2. Manager (`Manager`) — PLANNED / FUTURE SCOPE

- **Database Enum**: `'Manager'`
- **Current Status**: Defined in database schema; distinct authorization logic planned for future release.
- **Target Responsibility**: Review and approve submitted purchase requests and high-value purchase orders.

### 3. Employee (`Employee`) — PLANNED / FUTURE SCOPE

- **Database Enum**: `'Employee'`
- **Current Status**: Defined in database schema; distinct authorization logic planned for future release.
- **Target Responsibility**: Submit purchase requisitions and view personal request status.

---

## 3. Current vs. Target Role Summary

| Role Name | DB ENUM Value | Review-I Status | Primary Responsibility |
| --- | --- | --- | --- |
| System Administrator | `Admin` | ✅ Implemented | Full system overview, procurement oversight, operations dashboard |
| Manager | `Manager` | ⏳ Planned | Purchase request approval and budget authorization |
| Employee | `Employee` | ⏳ Planned | Purchase requisition submission |

---

## 4. Role-Based Access Control (RBAC) Architecture

- **Token Payload**: The user's role is embedded in the signed JWT token (`req.user.role`).
- **Middleware Integration**: `authMiddleware.js` extracts and attaches `req.user` to Express requests.
- **Future RBAC Middleware**: Role restriction middleware (`requireRole('Admin')`) will be applied to mutation endpoints in future development cycles.