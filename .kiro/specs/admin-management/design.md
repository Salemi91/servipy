# Design: Admin Management

## Architecture Overview

The admin feature follows the existing project architecture:
- **Backend**: Vertical Slice architecture within `py.com.servipy`. Admin endpoints are added to existing slices (`professional`, `category`) rather than creating a separate `admin` package.
- **Frontend**: Angular 17+ standalone components under `features/administration/`, with services, route guards, and Tailwind CSS.

## Backend Design

### Security

All admin endpoints live under `/api/v1/admin/**` which is protected in `SecurityConfig`:
```java
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
```

No `@PreAuthorize` is needed on individual methods — the URL-based security handles it.

### Category Slice (`py.com.servipy.category`)

**Existing files (from main):**
- `domain/Category.java` — JPA entity (table: `categories`)
- `infrastructure/persistence/CategoryRepository.java` — Spring Data JPA
- `application/CategoryService.java` — Service with `findAllActive()`
- `application/dto/CategoryDto.java` — Response record
- `infrastructure/web/CategoryController.java` — Public GET `/api/v1/categories`

**Added for admin:**
- `application/dto/CreateCategoryRequest.java` — Validated request record
- `infrastructure/web/AdminCategoryController.java` — POST `/api/v1/admin/categories`
- `CategoryService.create(CreateCategoryRequest)` — New method in existing service

### Professional Slice (`py.com.servipy.professional`)

**Existing files (from main):**
- `domain/ProfessionalProfile.java` — JPA entity with `approvalStatus` field
- `domain/ApprovalStatus.java` — Enum: PENDING, APPROVED, REJECTED
- `infrastructure/persistence/ProfessionalProfileRepository.java` — Spring Data JPA

**Added for admin:**
- `application/AdminProfessionalService.java` — Service with `findPending()`, `approve(id)`, `reject(id)`
- `application/dto/ProfessionalAdminDto.java` — Response record for admin views
- `infrastructure/web/AdminProfessionalController.java` — REST controller at `/api/v1/admin/professionals`
- `ProfessionalProfileRepository.findByApprovalStatus()` — New query method added

### API Contracts

| Method | Path | Request Body | Response | Status |
|--------|------|-------------|----------|--------|
| GET | `/api/v1/admin/professionals/pending` | — | `ProfessionalAdminDto[]` | 200 |
| PATCH | `/api/v1/admin/professionals/{id}/approve` | — | `ProfessionalAdminDto` | 200 |
| PATCH | `/api/v1/admin/professionals/{id}/reject` | — | `ProfessionalAdminDto` | 200 |
| POST | `/api/v1/admin/categories` | `CreateCategoryRequest` | `CategoryDto` | 201 |

**ProfessionalAdminDto:**
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "phone": "0981123456",
  "description": "Plomero con experiencia",
  "approvalStatus": "PENDING",
  "createdAt": "2024-06-01T10:00:00Z"
}
```

**CreateCategoryRequest:**
```json
{
  "name": "Plomería",
  "icon": "wrench",
  "description": "Servicios de plomería"
}
```

## Frontend Design

### Route Structure

```
/admin (AdminDashboardComponent - layout with nav tabs)
├── /admin/categories (CategoryListComponent)
└── /admin/professionals (ProfessionalListComponent)
```

Protected by `authGuard` + `roleGuard` with `data: { roles: ['ADMIN'] }` in `app.routes.ts`.

### Components

| Component | Location | Description |
|-----------|----------|-------------|
| `AdminDashboardComponent` | `features/administration/` | Layout with tab navigation (Categorías, Profesionales) + `<router-outlet>` |
| `CategoryListComponent` | `features/administration/categories/` | Table + create modal |
| `ProfessionalListComponent` | `features/administration/professionals/` | Table + confirmation dialogs |

### Services

| Service | Location | Methods |
|---------|----------|---------|
| `AdminCategoryService` | `features/administration/services/` | `getCategories()`, `create(request)` |
| `AdminProfessionalService` | `features/administration/services/` | `getPending()`, `approve(id)`, `reject(id)` |

### Models

| Model | Location |
|-------|----------|
| `ProfessionalAdmin` | `shared/models/professional-admin.model.ts` |
| `Category` (existing) | `shared/models/category.model.ts` |

## Database

No new migrations needed — the `professional_profiles` table already has `approval_status` column and the `categories` table exists from V2 migration.

## Testing Strategy

- **Backend**: Unit tests with JUnit 5 + Mockito for `AdminProfessionalService` (5 tests)
- **Frontend**: Build verification (ng build passes)
- **Integration**: Manual testing via browser with ADMIN user
