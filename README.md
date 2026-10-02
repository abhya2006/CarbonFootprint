# EcoTrack

EcoTrack is a personal carbon-footprint tracking application with a Spring Boot REST API and a React dashboard. Users can register, record daily activities, get database-configured CO2e estimates, review their footprint, create goals, and view personalized insights. Organization admins see aggregated data for members of their organization.

## Project Structure

```text
backend/
  src/main/java/com/ecotrack/
    config/       Security, CORS, demo data initialization
    controller/   REST API endpoints
    dto/          Validated request and response records
    entity/       JPA entities
    exception/    Consistent API error responses
    repository/   Spring Data repositories and aggregate queries
    security/     JWT filter, token service, user details
    service/      Authentication, calculation, activity, goal, dashboard, organization
  src/main/resources/
    application.properties
    schema.sql
frontend/
  src/
    App.jsx, AuthContext.jsx, api.js, styles.css, main.jsx
```

## Stack

- Java 25+, Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Security, Bean Validation
- MySQL 8, Maven, Lombok, JJWT, springdoc OpenAPI
- React 18, Vite, React Router, Axios, Tailwind CSS, Recharts, Framer Motion, Lucide React

## Features

- JWT sign-in and registration with BCrypt password hashing and role-protected organization endpoints.
- CRUD for activities and goals, with user ownership enforced on reads, updates, and deletes.
- Emission calculations use active factor rows stored in `emission_factors`.
- Dashboard summaries, category breakdown, weekly trend, monthly comparison, platform benchmark, and data-driven recommendations.
- Deterministic milestone badges, weekly streak calculations, persisted weekly insight summaries, community footprint list, and membership-scoped organization aggregation.
- Profile name/email editing and an earned-badge view.
- Responsive landing, authentication, dashboard, activity history, goals, insights, community, profile, and organization pages.
- OpenAPI documentation at `/swagger-ui.html`.

## Requirements

- JDK 25 or newer
- Maven 3.9+
- Node.js 20+ and npm
- MySQL 8

## Database Setup

Create a local MySQL database (or allow Hibernate to create it using the default JDBC URL):

```sql
CREATE DATABASE ecotrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The JPA entities create/update the application tables on startup with `spring.jpa.hibernate.ddl-auto=update`. `backend/src/main/resources/schema.sql` is also provided as a reference schema; it is not run automatically. For production, use versioned migrations (Flyway or Liquibase) instead of Hibernate schema updates.

## Configuration

Spring Boot does not load `.env` files by itself. Set backend variables in the process environment before starting the API. Never commit real `.env` files or production secrets.

Backend variables:

| Variable | Purpose | Example or default |
| --- | --- | --- |
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/ecotrack?...` |
| `DB_USERNAME` | MySQL account | `root` |
| `DB_PASSWORD` | MySQL password | empty |
| `JWT_SECRET` | HMAC signing key, at least 32 bytes | required; generate a unique value |
| `JWT_EXPIRATION_MS` | Token lifetime | `86400000` |

Generate a fresh local key in PowerShell with Node.js:

```powershell
$env:JWT_SECRET = node -e "process.stdout.write(require('node:crypto').randomBytes(32).toString('base64'))"
```

Store production secrets in a secret manager and rotate them as required.

## Run Locally

For a quick local run without installing MySQL, open a PowerShell terminal at the project root and start the backend using the in-memory H2 profile:

```powershell
$env:JWT_SECRET = node -e "process.stdout.write(require('node:crypto').randomBytes(32).toString('base64'))"
Set-Location backend
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```

The H2 database resets when the backend restarts. In a second terminal, start the frontend:

```powershell
Set-Location frontend
npm ci
npm run dev
```

Open the Vite URL (normally `http://localhost:5173`). The API is at `http://localhost:8080/api`; Swagger UI is at `http://localhost:8080/swagger-ui.html`.

To use MySQL instead, start MySQL, create the `ecotrack` database, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from `backend/.env.example` in the backend process environment, then run `mvn spring-boot:run` without the H2 profile.

For a production frontend bundle:

```powershell
cd frontend
npm run build
```

For backend tests and packaging:

```powershell
cd backend
mvn test
mvn package
```

## Demo Accounts

The application inserts these local demo accounts on first startup. Each uses the development password `Demo@123`:

| Role | Email |
| --- | --- |
| User | `demo@ecotrack.com` |
| Admin | `admin@ecotrack.com` |
| Organization admin | `orgadmin@ecotrack.com` |

These are development/demo credentials only. Change or remove them before deploying any shared environment.

The seeded organization ("Canopy Studio") includes `orgadmin@ecotrack.com` (ORG_ADMIN), `demo@ecotrack.com` (member), and `admin@ecotrack.com` (ORG_ADMIN) so the organization endpoints are viewable from any admin account.

## API Overview

All endpoints except registration and login require `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Create a USER account |
| POST | `/api/auth/login` | Sign in and receive a JWT |
| GET | `/api/auth/me` | Current account |
| PUT | `/api/auth/me` | Update account name and email |
| POST | `/api/activities` | Calculate and save an activity |
| POST | `/api/activities/calculate` | Preview CO2e for an activity |
| GET, GET by ID | `/api/activities` | List activities; optional `category`, `from`, `to` filters |
| PUT, DELETE | `/api/activities/{id}` | Update or remove an owned activity |
| GET | `/api/dashboard/summary` | Today/week/month metrics and goal progress |
| GET | `/api/dashboard/category-breakdown` | Category totals and shares |
| GET | `/api/dashboard/weekly-trend` | Current-week daily totals |
| GET | `/api/dashboard/monthly-comparison` | Current and previous month totals |
| GET | `/api/dashboard/benchmark` | Seven-day platform comparison |
| GET | `/api/dashboard/insights` | Personalized category observations and suggestions |
| GET | `/api/insights/weekly` | Persisted current/previous-week summary and recommendation |
| POST, GET | `/api/goals` | Create and list goals |
| GET, PUT, DELETE | `/api/goals/{id}` | Read, update, or remove an owned goal |
| GET | `/api/leaderboard` | Current-week aggregate list |
| GET | `/api/badges` | Current user's earned badges |
| GET | `/api/organization/dashboard` | Member-scoped organization totals (ORG_ADMIN/ADMIN) |
| GET | `/api/organization/members` | Organization members (ORG_ADMIN/ADMIN) |
| GET | `/api/organization/footprint` | Organization footprint (ORG_ADMIN/ADMIN) |
| GET | `/api/organization/trends` | Organization trend view (ORG_ADMIN/ADMIN) |

Validation and API errors use a JSON body with `timestamp`, `status`, `message`, and `path` fields.

## Emission Calculations

Each active factor is selected by category, activity type, and unit. The service multiplies the submitted quantity by that factor and rounds the CO2e estimate to two decimal places using decimal half-up rounding. Seed factors cover car, bus, train, bike, walking, electricity, beef, chicken, vegetarian and vegan meals, clothing, electronics, furniture, and general purchases.

Emission factors are configurable reference values, not authoritative measurements. Verify them against the latest authoritative datasets, geography, energy mix, and reporting methodology before using EcoTrack for real-world reporting. Seed factors are illustrative and source metadata identifies them as such.

## Tests

Backend JUnit/Mockito tests cover factor-based emission calculation (including unsupported activities and rounding) and dashboard summary aggregation:

```powershell
cd backend
mvn test
```

The frontend production bundle can be checked with `npm run build`. A dedicated frontend unit-test suite and automated browser suite are not included yet.

## Known Limitations

- MySQL is required for normal backend startup; no embedded database is configured.
- Demo seed values are illustrative. Replace factors and source metadata with reviewed region-specific data before reporting.
- JWTs are kept in `sessionStorage` by the browser app; a same-origin HttpOnly cookie and CSRF strategy would be preferable for a production deployment.
- Activity history currently filters and searches client-side after fetching the user's list; pagination and server-side sort/filter should be added for large accounts.
- The organization trend endpoint currently returns an aggregate dashboard snapshot rather than a full month-by-month series; department-level membership is not modeled.
- Full admin factor-management workflows are not implemented; factors are database-configurable and can be managed directly in the database for local evaluation.
- Error responses for framework-level authorization failures may use Spring Security's default response; production API error formatting should also be configured at the security entry point.
- No refresh-token/revocation flow, email verification, password reset, audit trail, or Flyway migrations are included.

## Future Improvements

- Add database migrations, refresh-token rotation, account recovery, and production-grade cookie-based authentication.
- Add server-side pagination, sortable filters, retention controls, and aggregate query indexes tuned against production data.
- Add scheduled weekly insight generation, editable reference factors behind ADMIN authorization, and automated badge/goal notification delivery.
- Expand organization reporting to time-series and team-specific cohorts with privacy thresholds.
- Add integration tests with Testcontainers for MySQL, plus React component and Playwright workflows.
- Build a reviewed factor catalog with geography, lifecycle scope, source URL, versioning, and change history.