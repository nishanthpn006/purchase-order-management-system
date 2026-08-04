# System Architecture

## Introduction

The Purchase Order Management System (POMS) follows a Three-Tier Architecture to ensure scalability, maintainability, security, and clear separation of responsibilities. Each layer performs a specific role and communicates only with the adjacent layer.

---

## Architecture Overview

The application is divided into three major layers:

1. Presentation Layer (Frontend)
2. Business Logic Layer (Backend)
3. Data Layer (Database)

This architecture minimizes coupling between components and makes future maintenance easier.

---

## Three-Tier Architecture

### 1. Presentation Layer

The Presentation Layer provides the user interface through which users interact with the system.

**Technology**

- React
- TypeScript
- Tailwind CSS
- Shadcn UI

**Responsibilities**

- Display dashboards and forms
- Validate basic user input
- Send API requests
- Display server responses

---

### 2. Business Logic Layer

The Business Logic Layer processes requests received from the frontend.

**Technology**

- Node.js
- Express.js
- Prisma ORM

**Responsibilities**

- Authenticate users
- Authorize user roles
- Validate business rules
- Process purchase requests
- Generate purchase orders
- Handle approvals
- Communicate with the database

---

### 3. Data Layer

The Data Layer stores all application data securely.

**Technology**

- PostgreSQL
- Prisma ORM

**Stores**

- Users
- Roles
- Vendors
- Products
- Purchase Requests
- Purchase Orders
- Approval Records
- Audit Logs

---

## Technology Stack

| Layer | Technology |
|--------|------------|
| Frontend | React, TypeScript, Tailwind CSS, Shadcn UI |
| Backend | Node.js, Express.js |
| Database | PostgreSQL |
| ORM | Prisma |
| Authentication | JWT |
| Version Control | Git & GitHub |
| Deployment | Vercel, Render/Railway |

---

## System Components

The system consists of the following major components:

- Authentication Module
- User Management Module
- Purchase Request Module
- Approval Management Module
- Vendor Management Module
- Purchase Order Module
- Reporting Module

---

## Data Flow

1. User logs into the application.
2. React sends requests to the Express API.
3. Express validates the request.
4. Business logic is executed.
5. Prisma communicates with PostgreSQL.
6. Database returns data.
7. Express sends the response.
8. React updates the user interface.

---

## Advantages

- Separation of concerns
- Improved maintainability
- Better scalability
- Enhanced security
- Easier testing
- Modular development
- Cleaner codebase

---

## Conclusion

The Three-Tier Architecture provides a solid foundation for building a scalable and secure Purchase Order Management System. It enables independent development of the frontend, backend, and database while supporting future enhancements and easier maintenance.