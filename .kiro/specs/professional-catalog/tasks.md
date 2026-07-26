# Implementation Plan: Catálogo Público de Profesionales

## Overview

Implementación del módulo de catálogo público que permite a visitantes explorar profesionales activos y aprobados, filtrar por categoría, buscar por texto y consultar perfiles detallados. El backend expone endpoints REST públicos (Spring Boot + JPA) y el frontend renderiza el catálogo con Angular standalone components, signals y Tailwind CSS.

## Tasks

- [x] 1. Database migration and JPA entities
  - [x] 1.1 Create Flyway migration V2__catalog_tables.sql
    - Create file `backend/src/main/resources/db/migration/V2__catalog_tables.sql`
    - Define tables: countries, cities, categories, users, professional_profiles, offered_services
    - Add foreign keys, indexes for approval_status, active flags, and category filtering
    - Use InnoDB engine with utf8mb4 charset
    - _Requirements: 1.1, 2.1, 4.1_

  - [x] 1.2 Create domain entities and enums
    - Create `py.com.servipy.user.domain.User` entity with Role enum
    - Create `py.com.servipy.user.domain.Role` enum (CLIENT, PROFESSIONAL, ADMIN)
    - Create `py.com.servipy.country.domain.Country` entity
    - Create `py.com.servipy.city.domain.City` entity with ManyToOne to Country
    - Create `py.com.servipy.category.domain.Category` entity
    - Create `py.com.servipy.professional.domain.ApprovalStatus` enum (PENDING, APPROVED, REJECTED)
    - Create `py.com.servipy.professional.domain.Availability` enum (PRESENCIAL, VIRTUAL, AMBOS)
    - Create `py.com.servipy.professional.domain.ProfessionalProfile` entity with relationships
    - Create `py.com.servipy.professional.domain.OfferedService` entity with relationships
    - _Requirements: 1.1, 1.2, 4.1_

  - [x] 1.3 Create repositories
    - Create `ProfessionalProfileRepository` extending JpaRepository and JpaSpecificationExecutor with custom `findActiveById` query using fetch joins
    - Create `OfferedServiceRepository` extending JpaRepository
    - Create `CategoryRepository` extending JpaRepository with `findByActiveTrueOrderByNameAsc`
    - _Requirements: 1.1, 2.4, 4.1_

- [x] 2. Backend services and specification
  - [x] 2.1 Implement ProfessionalSpecification
    - Create `py.com.servipy.professional.application.spec.ProfessionalSpecification`
    - Implement static `build(Long categoryId, String search)` method
    - Implement predicates: isApproved, userIsActive, hasActiveServices, inCategory, matchesSearch
    - matchesSearch must check user.name, profile.description, and active offeredService.name (case-insensitive)
    - Return null for nullable/blank parameters (JPA Specification ignores nulls)
    - _Requirements: 1.1, 2.1, 3.1, 3.2, 3.4_

  - [x] 2.2 Implement DTOs
    - Create `ProfessionalSummaryDto` record (id, name, professionalTitle, categoryName, description, cityName, referencePrice, availability, photoUrl)
    - Create `ProfessionalDetailDto` record (id, name, photoUrl, phone, whatsapp, description, cityName, availability, services)
    - Create `OfferedServiceDto` record (id, name, description, price, currency, categoryName)
    - Create `CategoryDto` record (id, name, icon, description)
    - _Requirements: 1.2, 4.1_

  - [x] 2.3 Implement ProfessionalCatalogService
    - Create `py.com.servipy.professional.application.ProfessionalCatalogService`
    - Implement `findAll(Long categoryId, String search, Pageable pageable)` returning Page<ProfessionalSummaryDto>
    - Implement `findById(Long id)` returning ProfessionalDetailDto, throwing ResourceNotFoundException if not found
    - Implement `toSummaryDto` mapping: truncate description to 150 chars with "...", calculate referencePrice as min price, professionalTitle as first active service name
    - Implement `toDetailDto` mapping with full description and active services list
    - _Requirements: 1.1, 1.2, 1.7, 2.1, 3.1, 4.1, 4.2_

  - [x] 2.4 Implement CategoryService
    - Create `py.com.servipy.category.application.CategoryService`
    - Implement `findAllActive()` returning List<CategoryDto> mapped from active categories
    - _Requirements: 2.4_

  - [x]* 2.5 Write unit tests for ProfessionalCatalogService
    - Create `ProfessionalCatalogServiceTest` using JUnit 5 + Mockito
    - Test: should_returnOnlyApprovedProfessionals_when_listAll
    - Test: should_capSizeTo50_when_sizeExceeds50 (via controller cap logic)
    - Test: should_filterByCategory_when_categoryIdProvided
    - Test: should_returnEmpty_when_categoryIdNotExists
    - Test: should_filterBySearch_when_searchProvided
    - Test: should_applyBothFilters_when_searchAndCategoryProvided
    - Test: should_ignoreSearch_when_searchIsBlank
    - Test: should_returnDetail_when_professionalIsActive
    - Test: should_throw404_when_professionalNotFound
    - Test: should_throw404_when_professionalNotApproved
    - Test: should_truncateDescription_when_exceedsMaxLength
    - Test: should_returnMinPrice_when_multipleServicesExist
    - _Requirements: 1.1, 1.2, 1.6, 2.1, 2.2, 3.1, 3.2, 3.4, 4.1, 4.2_

  - [x]* 2.6 Write unit tests for CategoryService
    - Create `CategoryServiceTest` using JUnit 5 + Mockito
    - Test: should_returnOnlyActiveCategories_when_listAll
    - Test: should_returnEmptyList_when_noCategoriesActive
    - _Requirements: 2.4_

  - [ ]* 2.7 Write property tests for ProfessionalCatalogService
    - **Property 1: Visibility filter** — Generate random professionals with varying active/approval/service states; assert only Active_Professionals appear in results
    - **Property 2: Summary DTO correctness** — Generate random profiles with active services; assert description <= 150 chars, referencePrice = min price, professionalTitle = first active service name
    - **Property 3: Page size capping** — Generate random size values; assert effective size is min(size, 50) or default 12 for size < 1
    - **Property 5: Category filter correctness** — Generate professionals with mixed categories; assert filtered results all have active service in specified category
    - **Property 8: Search filter correctness** — Generate professionals with varied names/descriptions; assert search results match in user.name, description, or service name
    - **Property 9: Whitespace search normalization** — Generate whitespace-only strings; assert same results as no search
    - **Validates: Requirements 1.1, 1.2, 1.6, 2.1, 3.1, 3.4**

- [x] 3. Backend controllers and error handling
  - [x] 3.1 Create ResourceNotFoundException and modify GlobalExceptionHandler
    - Create `py.com.servipy.shared.exception.ResourceNotFoundException` extending RuntimeException
    - Add handler for `ResourceNotFoundException` returning 404 with code "PROFESSIONAL_NOT_FOUND"
    - Add handler for `MethodArgumentTypeMismatchException` returning 400 with code "INVALID_PARAMETER"
    - _Requirements: 2.3, 4.2, 4.3_

  - [x] 3.2 Implement ProfessionalCatalogController
    - Create `py.com.servipy.professional.infrastructure.web.ProfessionalCatalogController`
    - Implement GET /api/v1/professionals with params: categoryId (optional Long), search (optional String), page (default 0), size (default 12)
    - Cap size at 50 (min 1), sort by user.name ascending
    - Implement GET /api/v1/professionals/{id} returning ProfessionalDetailDto
    - _Requirements: 1.1, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 3.1, 4.1, 4.2_

  - [x] 3.3 Implement CategoryController
    - Create `py.com.servipy.category.infrastructure.web.CategoryController`
    - Implement GET /api/v1/categories returning List<CategoryDto>
    - _Requirements: 2.4_

  - [x] 3.4 Modify SecurityConfig to whitelist catalog endpoints
    - Add `.requestMatchers(HttpMethod.GET, "/api/v1/professionals/**").permitAll()`
    - Add `.requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()`
    - Import `HttpMethod` in SecurityConfig
    - _Requirements: 7.1, 7.2, 7.4_

  - [x] 3.5 Add jqwik dependency to pom.xml
    - Add jqwik 1.8.5 with test scope to backend pom.xml
    - _Requirements: Testing infrastructure_

  - [x]* 3.6 Write controller integration tests
    - Create `ProfessionalCatalogControllerTest` using MockMvc
    - Test: should_return200_when_getProfessionalsWithoutAuth
    - Test: should_return200_when_getCategoriesWithoutAuth
    - Test: should_return400_when_categoryIdIsNonNumeric
    - Test: should_return400_when_professionalIdIsNonNumeric
    - Test: should_return404_when_professionalIdNotFound
    - _Requirements: 2.3, 4.2, 4.3, 7.1_

  - [ ]* 3.7 Write property tests for controller/error handling
    - **Property 4: Sort order invariant** — Assert page results are always sorted ascending by name
    - **Property 6: Invalid categoryId rejection** — Generate non-positive/non-numeric categoryId values; assert 400 response
    - **Property 7: Active categories only** — Assert all returned categories have active = true
    - **Property 10: Non-active professional detail returns 404** — Generate ids of non-active professionals; assert 404
    - **Validates: Requirements 1.7, 2.3, 2.4, 4.2**

- [x] 4. Checkpoint - Backend verification
  - Ensure all tests pass with `./mvnw test`. Ask the user if questions arise.

- [x] 5. Frontend models and services
  - [x] 5.1 Create shared TypeScript models
    - Create `src/app/shared/models/professional.model.ts` with ProfessionalSummary, ProfessionalDetail, OfferedServiceItem interfaces
    - Create `src/app/shared/models/category.model.ts` with Category interface
    - Create `src/app/shared/models/paginated-response.model.ts` with generic PaginatedResponse<T> interface
    - _Requirements: 1.2, 1.4, 2.4, 4.1_

  - [x] 5.2 Implement CatalogService
    - Create `src/app/features/public/catalog/services/catalog.service.ts`
    - Implement `getProfessionals(params)` calling GET /professionals with query string building
    - Implement `getProfessionalById(id)` calling GET /professionals/:id
    - Implement `getCategories()` calling GET /categories
    - Use existing `ApiService` for HTTP calls
    - _Requirements: 1.3, 2.1, 2.6, 3.1, 4.1_

  - [ ]* 5.3 Write unit tests for CatalogService
    - Create `catalog.service.spec.ts` using HttpClientTestingModule
    - Test: should call GET /professionals with default params
    - Test: should include categoryId when provided
    - Test: should include search when provided
    - Test: should call GET /professionals/:id
    - Test: should call GET /categories
    - _Requirements: 1.3, 2.1, 3.1, 4.1, 2.4_

- [x] 6. Frontend UI components
  - [x] 6.1 Create presentational sub-components
    - Create `professional-card` component (standalone, inputs: ProfessionalSummary, responsive card with Tailwind)
    - Create `category-filter` component (standalone, inputs: categories array, output: categorySelected event)
    - Create `search-bar` component (standalone, output: searchChanged event with debounce)
    - Create `empty-state` component (standalone, inputs: hasFilters boolean, output: clearFilters event)
    - Create `loading-state` component (standalone, skeleton placeholders)
    - Create `error-state` component (standalone, output: retry event)
    - _Requirements: 1.2, 2.5, 2.6, 3.1, 5.1, 5.2, 6.1, 6.2, 6.3, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [x] 6.2 Implement CatalogListComponent
    - Create `catalog-list` component (standalone) with signal-based state management
    - Manage CatalogState: 'loading' | 'loaded' | 'error'
    - Wire category-filter, search-bar, professional-card grid, empty-state, loading-state, error-state
    - Implement responsive grid: 1 col (< 640px), 2 cols (640-1023px), 3 cols (>= 1024px) with Tailwind
    - Handle pagination (load more or paginator)
    - Implement retry logic on error
    - _Requirements: 1.2, 1.3, 2.5, 2.6, 3.1, 5.1, 5.2, 5.3, 6.1, 6.2, 6.3, 6.4, 8.1, 8.2, 8.3_

  - [x] 6.3 Implement CatalogDetailComponent
    - Create `catalog-detail` component (standalone) with signal-based state
    - Read route param `:id`, call CatalogService.getProfessionalById
    - Display full professional detail: photo, name, phone, whatsapp, description, city, availability
    - Display list of offered services with price and category
    - Handle 404 (professional not found) and error states
    - _Requirements: 4.1, 4.2, 6.1, 6.2_

  - [x] 6.4 Configure catalog routes and integrate into PUBLIC_ROUTES
    - Create `src/app/features/public/catalog/catalog.routes.ts` with CATALOG_ROUTES
    - Route '' → CatalogListComponent (lazy-loaded)
    - Route ':id' → CatalogDetailComponent (lazy-loaded)
    - Modify `src/app/features/public/routes.ts` to add path 'profesionales' loading CATALOG_ROUTES
    - _Requirements: 7.3_

  - [ ]* 6.5 Write unit tests for CatalogListComponent
    - Test: should show loading state while fetching
    - Test: should show professional cards on success
    - Test: should show empty state when no results and no filters
    - Test: should show empty state with clear action when filtered
    - Test: should show error state on HTTP failure
    - Test: should retry same request on retry button click
    - Test: should filter by category when selected
    - Test: should not show empty and loading simultaneously
    - _Requirements: 1.2, 2.6, 5.1, 5.2, 6.1, 6.2, 6.3, 6.4_

  - [ ]* 6.6 Write unit tests for CatalogDetailComponent
    - Test: should display professional detail on success
    - Test: should show error when professional not found
    - _Requirements: 4.1, 4.2_

  - [ ]* 6.7 Write property test for UI state mutual exclusion (fast-check)
    - Add `fast-check` as devDependency in frontend package.json
    - **Property 11: UI state mutual exclusion** — Generate random sequences of loading/loaded/error transitions; assert exactly one state is displayed at any time
    - **Validates: Requirements 5.3, 6.1, 6.4**

- [x] 7. Final checkpoint - Full verification
  - Ensure all backend tests pass with `./mvnw test` and frontend tests pass with `ng test --watch=false`. Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Backend tasks (1-4) must complete before frontend tasks (5-7) since the frontend depends on the API
- The design uses Java (backend) and TypeScript/Angular (frontend) — no language selection needed

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["1.3", "2.2"] },
    { "id": 2, "tasks": ["2.1", "2.3", "2.4", "3.1"] },
    { "id": 3, "tasks": ["2.5", "2.6", "2.7", "3.2", "3.3", "3.5"] },
    { "id": 4, "tasks": ["3.4", "3.6", "3.7"] },
    { "id": 5, "tasks": ["5.1"] },
    { "id": 6, "tasks": ["5.2", "6.1"] },
    { "id": 7, "tasks": ["5.3", "6.2", "6.3", "6.4"] },
    { "id": 8, "tasks": ["6.5", "6.6", "6.7"] }
  ]
}
```
