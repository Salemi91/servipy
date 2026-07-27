# Requirements: Admin Management

## Introduction

Feature de administración que permite a usuarios con rol ADMIN gestionar el contenido de la plataforma ServiPy. Incluye la moderación de profesionales pendientes de aprobación y la gestión (creación y listado) de categorías de servicio.

## Glossary

- **ADMIN**: Usuario con rol administrativo que modera contenido y usuarios.
- **ProfessionalProfile**: Perfil de un profesional registrado, con estado de aprobación (PENDING, APPROVED, REJECTED).
- **Category**: Categoría de servicio (ej: Plomería, Electricidad) bajo la cual los profesionales ofrecen sus servicios.
- **ApprovalStatus**: Estado de aprobación de un profesional: PENDING → APPROVED o REJECTED.

## Requirements

### Requirement 1: Admin Route Protection

**User Story:** As an ADMIN user, I want the admin panel to be protected so that only authenticated users with ROLE_ADMIN can access it.

#### Acceptance Criteria

1. WHEN a user navigates to `/admin/*` routes WITHOUT authentication THEN they are redirected to `/login`.
2. WHEN an authenticated user with role CLIENT or PROFESSIONAL navigates to `/admin/*` THEN they are redirected to `/`.
3. WHEN an authenticated user with role ADMIN navigates to `/admin/*` THEN access is granted.
4. The backend endpoints under `/api/v1/admin/**` SHALL require ROLE_ADMIN via SecurityConfig.
5. The frontend uses `authGuard` + `roleGuard` with `data: { roles: ['ADMIN'] }` to protect the `/admin` route.

### Requirement 2: Admin Login Redirection

**User Story:** As an ADMIN user, I want to be redirected to `/admin` after login so that I land directly in the admin panel.

#### Acceptance Criteria

1. WHEN a user with role ADMIN logs in successfully THEN the frontend redirects to `/admin`.
2. WHEN a user with role CLIENT logs in THEN redirect to `/client`.
3. WHEN a user with role PROFESSIONAL logs in THEN redirect to `/professional`.

### Requirement 3: Category Listing

**User Story:** As an ADMIN, I want to see all active categories in a table so that I can manage the service taxonomy.

#### Acceptance Criteria

1. The admin panel SHALL display a table at `/admin/categories` showing: name, description, icon.
2. Categories are fetched via `GET /api/v1/categories` (public endpoint).
3. The table SHALL show a loading state while fetching.
4. IF no categories exist THEN a message "No hay categorías registradas" is shown.

### Requirement 4: Category Creation

**User Story:** As an ADMIN, I want to create new categories so that professionals can offer services under them.

#### Acceptance Criteria

1. The admin panel SHALL have a "+ Nueva Categoría" button that opens a modal form.
2. The form requires: name (mandatory), icon (optional, max 50 chars), description (optional, max 255 chars).
3. On submit, the frontend calls `POST /api/v1/admin/categories` with the form data.
4. The backend SHALL validate the request with Bean Validation (@NotBlank name).
5. On success, the modal closes and the category list refreshes.
6. On error, the modal shows an error message.
7. The endpoint returns HTTP 201 Created with the created category DTO.

### Requirement 5: Pending Professional Listing

**User Story:** As an ADMIN, I want to see professionals pending approval so that I can moderate who joins the platform.

#### Acceptance Criteria

1. The admin panel SHALL display a table at `/admin/professionals` showing: name, email, phone, description, registration date, action buttons.
2. Professionals are fetched via `GET /api/v1/admin/professionals/pending`.
3. Only professionals with `approval_status = 'PENDING'` are returned.
4. IF no pending professionals exist THEN a message is shown.

### Requirement 6: Professional Approval

**User Story:** As an ADMIN, I want to approve a pending professional so that they can offer services on the platform.

#### Acceptance Criteria

1. Each row in the professionals table SHALL have an "Aprobar" button (green).
2. Clicking "Aprobar" SHALL show a confirmation dialog with the professional's name.
3. On confirm, the frontend calls `PATCH /api/v1/admin/professionals/{id}/approve`.
4. The backend changes `approval_status` to `APPROVED` and updates `updated_at`.
5. On success, the list refreshes (the approved professional disappears from pending).
6. IF the professional ID does not exist THEN the backend returns 404.

### Requirement 7: Professional Rejection

**User Story:** As an ADMIN, I want to reject a pending professional so that unsuitable profiles don't appear in the catalog.

#### Acceptance Criteria

1. Each row SHALL have a "Rechazar" button (red).
2. Clicking "Rechazar" SHALL show a confirmation dialog.
3. On confirm, the frontend calls `PATCH /api/v1/admin/professionals/{id}/reject`.
4. The backend changes `approval_status` to `REJECTED` and updates `updated_at`.
5. On success, the list refreshes.
6. IF the professional ID does not exist THEN the backend returns 404.

## Correctness Properties

1. **Authorization Invariant**: All `/api/v1/admin/**` endpoints MUST reject requests without a valid JWT containing ROLE_ADMIN.
2. **State Transition**: A professional's approval_status can only transition PENDING → APPROVED or PENDING → REJECTED via admin action.
3. **Data Integrity**: Category creation always sets `active = true` by default.
4. **Idempotency**: Approving an already-approved professional does not cause errors (it updates the timestamp).
