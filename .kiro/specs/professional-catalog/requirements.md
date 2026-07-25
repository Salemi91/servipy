# Requirements Document

## Introduction

Catálogo público de profesionales para ServiPy. Esta feature permite a cualquier visitante (sin autenticación) explorar los profesionales disponibles en la plataforma, filtrar por categoría, buscar por texto y consultar el detalle público de un profesional. El objetivo es ofrecer una experiencia de descubrimiento fluida que conecte visitantes con profesionales aprobados y activos en Paraguay.

## Glossary

- **Catalog_API**: Conjunto de endpoints REST bajo `/api/v1` que exponen datos públicos de profesionales y categorías.
- **Professional_Card**: Representación resumida de un profesional en el listado del catálogo (nombre, título, categoría, ciudad, precio de referencia, modalidad, imagen).
- **Professional_Detail**: Representación completa del perfil público de un profesional, incluyendo todos los campos de Professional_Card más descripción completa y servicios ofrecidos.
- **Category_Filter**: Mecanismo que permite al visitante filtrar profesionales por una categoría específica.
- **Text_Search**: Mecanismo de búsqueda textual que filtra profesionales por coincidencia en nombre, título profesional o descripción.
- **Visitor**: Usuario no autenticado que navega el catálogo público.
- **Active_Professional**: Profesional cuyo usuario tiene `active = true`, cuyo perfil tiene `approvalStatus = APPROVED` y que tiene al menos un servicio activo.
- **Catalog_Frontend**: Módulo Angular del frontend responsable de renderizar el catálogo de profesionales.
- **Empty_State**: Pantalla o componente que se muestra cuando una consulta al catálogo no retorna resultados.
- **Loading_State**: Indicador visual que se muestra mientras se realiza una petición HTTP al backend.
- **Error_State**: Pantalla o componente que se muestra cuando una petición HTTP falla.

## Requirements

### Requirement 1: Listar profesionales públicos

**User Story:** Como visitante, quiero ver un listado de profesionales activos y aprobados, para poder descubrir opciones de servicio disponibles en Paraguay.

#### Acceptance Criteria

1. WHEN a Visitor requests the professional catalog, THE Catalog_API SHALL return only professionals whose user has active = true, whose ProfessionalProfile has approvalStatus = APPROVED, and who have at least one OfferedService with active = true.
2. THE Catalog_API SHALL return for each professional: identifier, name, professional title (from the first active OfferedService name), category name, brief description (truncated to 150 characters), city name, reference price (lowest price among active OfferedServices), service modality (availability field), and photo URL (or null if not set).
3. WHEN a Visitor requests the professional catalog without specifying page or size parameters, THE Catalog_API SHALL return results paginated with a default page of 0 and size of 12 items.
4. THE Catalog_API SHALL include in the paginated response: totalElements, totalPages, current page number (number), page size (size), and a content array with the professional items.
5. WHEN a Visitor requests a page number that exceeds the total pages, THE Catalog_API SHALL return an empty content array with the correct pagination metadata (totalElements, totalPages unchanged).
6. THE Catalog_API SHALL accept a size parameter between 1 and 50; IF size exceeds 50, THEN the API SHALL cap it at 50.
7. THE Catalog_API SHALL sort results by name ascending by default.

### Requirement 2: Filtrar por categoría

**User Story:** Como visitante, quiero filtrar el catálogo de profesionales por categoría, para encontrar rápidamente el tipo de servicio que necesito.

#### Acceptance Criteria

1. WHEN a Visitor provides a valid categoryId query parameter on GET /api/v1/professionals, THE Catalog_API SHALL return only professionals whose approvalStatus is APPROVED and who have at least one active OfferedService in the specified category, paginated using the same page/size defaults as the unfiltered listing.
2. IF a Visitor provides a categoryId that does not match any existing category, THEN THE Catalog_API SHALL return an empty content array with pagination metadata showing zero total elements.
3. IF a Visitor provides a categoryId that is not a positive integer, THEN THE Catalog_API SHALL return HTTP 400 with an error response following the standard error format indicating that the categoryId parameter is invalid.
4. THE Catalog_API SHALL expose a GET /api/v1/categories endpoint that returns all categories where active is true, each containing id, name, icon, and description.
5. THE Catalog_Frontend SHALL display available categories as selectable filter options retrieved from the categories endpoint, and SHALL display a message indicating no results found when the filtered response contains zero professionals.
6. WHEN a Visitor selects a category filter, THE Catalog_Frontend SHALL send the categoryId query parameter to the professionals endpoint and update the displayed list with the filtered results without a full page reload.

### Requirement 3: Búsqueda textual

**User Story:** Como visitante, quiero buscar profesionales por nombre, título o descripción, para encontrar un profesional específico o un servicio particular.

#### Acceptance Criteria

1. WHEN a Visitor provides a search query parameter with at least 1 non-whitespace character and at most 100 characters, THE Catalog_API SHALL return only professionals with approvalStatus APPROVED whose User name, active OfferedService name, or ProfessionalProfile description contains the search term (case-insensitive partial match).
2. WHEN a Visitor provides both a search query and a categoryId, THE Catalog_API SHALL apply both filters simultaneously, returning only APPROVED professionals that match the search term and have at least one active OfferedService in the specified category.
3. WHEN a Visitor provides a search term that matches no APPROVED professionals, THE Catalog_API SHALL return an empty content array with pagination metadata showing zero total elements.
4. IF a Visitor provides an empty, whitespace-only, or missing search query parameter, THEN THE Catalog_API SHALL ignore the search filter and return results as if no search term was provided.

### Requirement 4: Detalle de profesional

**User Story:** Como visitante, quiero ver el perfil completo de un profesional, para evaluar si es adecuado para el servicio que necesito.

#### Acceptance Criteria

1. WHEN a Visitor requests a professional detail by id, THE Catalog_API SHALL return the full public profile including: identifier, name, photo URL (or null if not set), phone, whatsapp (or null if not set), description, city name, availability, and a list of active offered services each containing: service identifier, name, description, price, currency, and category name.
2. IF the requested id does not correspond to an Active_Professional or does not exist, THEN THE Catalog_API SHALL return HTTP 404 with an error response following the standard error format indicating the professional was not found.
3. IF the requested id is not a valid numeric identifier, THEN THE Catalog_API SHALL return HTTP 400 with an error response following the standard error format indicating an invalid identifier.

### Requirement 5: Estado vacío

**User Story:** Como visitante, quiero ver un mensaje claro cuando no hay resultados, para entender que mi búsqueda no encontró profesionales y poder ajustar mis filtros.

#### Acceptance Criteria

1. WHEN the Catalog_API returns an empty content array and no Category_Filter or Text_Search is active, THE Catalog_Frontend SHALL display the Empty_State component with an illustrative icon and a message indicating no professionals are available at this time.
2. WHEN the Catalog_API returns an empty content array and a Category_Filter or Text_Search is active, THE Catalog_Frontend SHALL display the Empty_State component with a message indicating no professionals match the current criteria and a clickable action to clear all active filters.
3. THE Catalog_Frontend SHALL display the Empty_State component only after the HTTP response has been fully received (not during loading).

### Requirement 6: Estados de carga y error

**User Story:** Como visitante, quiero ver indicadores de carga y mensajes de error, para saber que la aplicación está procesando mi solicitud o que algo salió mal.

#### Acceptance Criteria

1. WHILE an HTTP request to the Catalog_API is in progress, THE Catalog_Frontend SHALL replace the content area with the Loading_State indicator (skeleton placeholders or spinner).
2. WHEN an HTTP request to the Catalog_API fails (network error or HTTP status >= 500), THE Catalog_Frontend SHALL replace the content area with the Error_State component displaying a user-friendly message (not raw HTTP codes) and a "Reintentar" button.
3. WHEN the Visitor clicks the "Reintentar" button in the Error_State, THE Catalog_Frontend SHALL re-execute the exact same HTTP request (same URL, query parameters, and page) that failed.
4. WHILE the Loading_State is displayed, THE Catalog_Frontend SHALL NOT display the Empty_State or Error_State simultaneously.

### Requirement 7: Acceso público sin autenticación

**User Story:** Como visitante, quiero acceder al catálogo sin necesidad de registrarme o iniciar sesión, para poder explorar los servicios disponibles libremente.

#### Acceptance Criteria

1. THE Catalog_API SHALL allow access to GET /api/v1/professionals, GET /api/v1/professionals/{id}, and GET /api/v1/categories without any authentication token, returning the same response structure as for authenticated users.
2. THE Catalog_API SHALL allow unauthenticated use of query parameters (categoryId, search, page, size) on GET /api/v1/professionals, returning filtered results without requiring an authentication token.
3. THE Catalog_Frontend SHALL not require login, display authentication prompts, or redirect to a login page when navigating the catalog listing or detail routes.
4. WHEN the SecurityFilterChain is configured, THE Catalog_API SHALL whitelist all catalog endpoints (GET /api/v1/professionals/**, GET /api/v1/categories) so that requests without an Authorization header receive a successful response rather than a 401 or 403 rejection.

### Requirement 8: Diseño responsive del catálogo

**User Story:** Como visitante, quiero que el catálogo se vea correctamente en dispositivos móviles, tablets y escritorio, para poder explorar profesionales desde cualquier dispositivo.

#### Acceptance Criteria

1. WHILE the viewport width is less than 640px (minimum supported width 320px), THE Catalog_Frontend SHALL display Professional_Card items in a single-column layout.
2. WHILE the viewport width is between 640px (inclusive) and 1024px (exclusive), THE Catalog_Frontend SHALL display Professional_Card items in a two-column grid layout.
3. WHILE the viewport width is 1024px or greater, THE Catalog_Frontend SHALL display Professional_Card items in a three-column grid layout.
4. THE Catalog_Frontend SHALL render interactive elements (buttons, links) with a minimum tap target size of 44x44 pixels, and text content with a minimum font size of 14px, across all supported viewport widths.
5. THE Catalog_Frontend SHALL scale images within Professional_Card to fit the card container width while preserving aspect ratio and without overflowing the card boundaries.
6. IF text content within a Professional_Card exceeds the available display area, THEN THE Catalog_Frontend SHALL truncate the overflowing text with an ellipsis indicator.
