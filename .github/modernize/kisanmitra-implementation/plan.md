# KisanMitra Implementation Plan

**Project**: Smart Farmer Procurement Queue Management System

## Technical Framework

- **Backend**: Java 21, Spring Boot 4.1.1, Maven
- **Frontend**: React 19, Vite 8, JavaScript
- **Database**: MySQL, accessed through Spring Data JPA
- **Security**: Spring Security, JWT authentication, role-based endpoint rules
- **Localization**: i18next with English and Hindi locale files
- **Current tests**: One Spring Boot context-load test; no frontend test script or
  checked-in frontend test files were found during planning.

## Evidence and Current State

The inventory below is based on the source present in the worktree at planning
time. It is not a claim that every feature is complete or production-ready.

- `FrontEnd/Vite-Project/src/App.jsx` defines login, registration, verification,
  dashboard, centers, slots, queue, procurements, payments, profile, and
  notifications routes. The shared workspace route requires an authenticated
  user; page presentation also varies by role.
- `User.Role` includes `FARMER`, `CENTER_OPERATOR`, and `ADMIN`.
  `SecurityConfig` explicitly limits dashboard reads to operators/admins,
  center and slot reads to all three roles, center/slot mutations to admins,
  queue actions to their configured farmer/operator roles, and procurement and
  payment management endpoints to operators/admins. Frontend route visibility
  is not a replacement for backend authorization.
- Farmer profile APIs exist at `/api/farmers/profile`; farmer booking, token,
  and queue APIs are present. The UI contains profile, booking, queue, and
  dashboard views, but their actual support and role behavior must be checked
  against the services and responses during implementation.
- The frontend currently renders a clear integration placeholder for a
  farmer's payment history. Its payment service calls `GET /api/payments`,
  while the backend security rules reserve payment listing for center
  operators/admins and the payment controller/service currently expose general
  payment listing and lookup operations without a farmer principal parameter.
- Existing relational associations provide a candidate ownership path through
  `Payment -> Procurement -> ProcurementToken -> Farmer -> User`. This makes a
  farmer-owned history query potentially implementable without changing the
  MySQL schema. The execution task must verify the association and use the
  authenticated account identity, never a caller-supplied farmer ID.
- Center addresses and farmer profile address/location text fields exist.
  No reverse-geocoding provider or configuration was found. Do not infer
  coordinates, call an unconfigured provider, or represent geocoding as
  available.
- The `ADMIN` role exists, but no admin bootstrap/provisioning flow was found
  in the inspected source. Do not add a hardcoded password or seed credentials.
- English and Hindi translation files exist. The selected slice must add or
  update user-visible strings in both locales without treating this as proof
  that every page is fully localized.
- `Backend/backend/src/main/resources/application.properties` sets JPA schema
  handling to `update`. No database schema change is currently planned.
  Preserve existing data; do not add destructive DDL or manually alter the
  database. If implementation evidence makes a schema change unavoidable,
  stop before applying it and propose a separate additive, reversible migration.

## Initial Worktree Baseline and Preservation

The initial `git status --short` showed pre-existing edits, staged changes were
not reported, and the branch was `main`. Preserve these changes exactly; do not
revert, overwrite, reformat, delete, or stage them as part of this plan:

- Modified: `FrontEnd/Vite-Project/README.md`,
  `FrontEnd/Vite-Project/index.html`,
  `FrontEnd/Vite-Project/public/manifest.webmanifest`,
  `FrontEnd/Vite-Project/src/components/AppShell.jsx`,
  `FrontEnd/Vite-Project/src/i18n/locales/en/translation.json`,
  `FrontEnd/Vite-Project/src/i18n/locales/hi/translation.json`,
  `FrontEnd/Vite-Project/src/main.jsx`,
  `FrontEnd/Vite-Project/src/pages/AuthPages.jsx`,
  `FrontEnd/Vite-Project/src/services/api.js`.
- Deleted: `FrontEnd/Vite-Project/public/favicon.svg`,
  `FrontEnd/Vite-Project/public/icons.svg`,
  `FrontEnd/Vite-Project/src/assets/hero.png`,
  `FrontEnd/Vite-Project/src/assets/react.svg`,
  `FrontEnd/Vite-Project/src/assets/vite.svg`.
- Untracked: root `README.md`.

The plan is saved in a new directory and does not modify those files. Recheck
the worktree before any implementation and distinguish subsequent changes
from this baseline.

## Overview and Scope

Deliver one evidence-backed critical vertical slice: a farmer can view only
their own payment history in the existing payments page, backed by an
authenticated Spring Boot API query over the existing MySQL relationships and
verified with regression tests. Preserve current operator/admin payment
management behavior unless tests establish a required compatible adjustment.

First inventory the current role, route, API, model, localization, and test
coverage. Audit profile, dashboard, center/location, booking, queue,
procurement/history, payments, and admin behavior; clearly label each as
implemented, partial, unavailable, or out of this slice. Do not turn that
audit into a blanket redesign or imply unsupported functionality is complete.

Admin bootstrap and reverse geocoding are conditional, conservative work:
there is no configured provider or existing bootstrap flow evidenced at
planning time. Keep geocoding unavailable until a provider is explicitly
configured. Add a one-time admin bootstrap only if the implementation can
enforce a safe, documented, non-hardcoded secret lifecycle; otherwise leave
provisioning disabled and report the secure operational prerequisite.

## Implementation Impact Summary

| Surface | Current evidence | Planned result | Data impact |
|---|---|---|---|
| Farmer payments | Farmer UI placeholder; list API is role-limited | Owner-scoped history | Existing relations; no schema change expected |
| Roles and data access | Backend role rules exist; per-center ownership needs audit | Verify and test API-side boundaries | No broad access-policy redesign |
| Admin bootstrap | No flow found; ADMIN role exists | Safe documented flow only if enforceable | No credential or seed account |
| Location | Address text exists; no provider found | Report reverse geocoding unavailable | No fabricated coordinates/provider |
| Adjacent features | Routes/APIs vary in coverage | Evidence-based status and scope report | No blanket feature expansion |

## Execution Gates and Validation

The detailed task breakdown, dependencies, and pass criteria are in
`.metadata/tasks.json`. Implementation must begin with the baseline and source
inventory, then deliver only the scoped vertical slice and explicitly requested
safe documentation. No external provider, API key, credential, sample
transaction, or database content may be invented. If a necessary access model,
provider choice, or provisioning decision cannot be resolved using the secure
defaults above, stop that sub-scope and request clarification rather than
guessing.

Run frontend lint and production build plus Maven tests and package/build when
feasible. Report the exact commands and classify each result as **PASS** or
**BLOCKED**, with the concrete reason for any blocked check. Report changed
files, API changes, schema changes (including none), deployment/configuration
requirements, and remaining unsupported scope. Never report a test as passed
unless it actually ran successfully.
