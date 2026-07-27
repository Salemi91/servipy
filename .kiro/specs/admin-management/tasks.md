# Tasks: Admin Management

- [x] 1. Auth Integration & Admin Route Guard
  - [x] 1.1 SecurityConfig already protects `/api/v1/admin/**` with `hasRole("ADMIN")` (existing in main).
  - [x] 1.2 Frontend route `/admin` protected by `authGuard` + `roleGuard` with `data: { roles: ['ADMIN'] }` (existing in main).
  - [x] 1.3 Login redirection by role: ADMIN → `/admin`, PROFESSIONAL → `/professional`, CLIENT → `/client` (existing LoginComponent).

- [x] 2. Category Management
  - [x] 2.1 Backend: Created `CreateCategoryRequest.java` (validated record DTO) in `category/application/dto/`.
  - [x] 2.2 Backend: Added `create(CreateCategoryRequest)` method to existing `CategoryService`.
  - [x] 2.3 Backend: Created `AdminCategoryController.java` at `/api/v1/admin/categories` with POST endpoint (201 Created).
  - [x] 2.4 Frontend: Created `AdminCategoryService` with `getCategories()` and `create()` methods.
  - [x] 2.5 Frontend: Created `CategoryListComponent` with table view and create modal (Tailwind CSS).

- [x] 3. Professional Approval Workflow
  - [x] 3.1 Backend: Added `findByApprovalStatus(ApprovalStatus)` method to `ProfessionalProfileRepository`.
  - [x] 3.2 Backend: Created `AdminProfessionalService` with `findPending()`, `approve(id)`, `reject(id)` methods.
  - [x] 3.3 Backend: Created `ProfessionalAdminDto` response record.
  - [x] 3.4 Backend: Created `AdminProfessionalController` at `/api/v1/admin/professionals` with GET /pending, PATCH /{id}/approve, PATCH /{id}/reject.
  - [x] 3.5 Backend: Created `AdminProfessionalServiceTest` with 5 unit tests (JUnit 5 + Mockito).
  - [x] 3.6 Frontend: Created `ProfessionalAdmin` model interface.
  - [x] 3.7 Frontend: Created `AdminProfessionalService` with `getPending()`, `approve()`, `reject()`.
  - [x] 3.8 Frontend: Created `ProfessionalListComponent` with table and confirmation dialogs.

- [x] 4. Admin Dashboard & Navigation
  - [x] 4.1 Frontend: Created `AdminDashboardComponent` with tab navigation (Categorías, Profesionales).
  - [x] 4.2 Frontend: Updated `ADMIN_ROUTES` with nested children under AdminDashboardComponent.
  - [x] 4.3 Frontend: Removed old `AdminPlaceholderComponent`.
