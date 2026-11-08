# KisanMitra · Smart Farmer Procurement

Responsive React + Vite frontend for the Smart Farmer Procurement & Queue Management System.

## Run locally

1. Start the Spring Boot backend on `http://localhost:8080`.
2. From `FrontEnd/Vite-Project`, install packages and start Vite:

   ```sh
   npm install
   npm run dev
   ```

3. Open the local URL printed by Vite. Requests to `/api` are proxied to the backend.

For a deployed frontend, copy `.env.example` to `.env` and set `VITE_API_BASE_URL` to the public API origin. This value is public configuration, not a place for secrets. The API must use HTTPS in production and allow the deployed frontend origin.

## Build and checks

```sh
npm run lint
npm run build
npm run preview
```

The production build includes an installable PWA. The service worker does not cache navigation pages or API responses; authentication screens and private data therefore remain network-only.

## Authentication and API scope

The backend issues the login JWT in an HttpOnly cookie and provides `/api/auth/session` for safe session restoration. The default cookie policy is `SameSite=Strict`; the frontend does not persist JWTs in browser storage or expose them to JavaScript. Cookie-authenticated write requests use Spring Security CSRF tokens; bearer-header clients remain supported. For production, set `AUTH_COOKIE_SECURE=true` and `FRONTEND_ORIGIN` to the exact HTTPS frontend origin. If frontend and API are on different sites, set `AUTH_COOKIE_SAME_SITE=None` as well as `AUTH_COOKIE_SECURE=true`. The frontend language choice is stored as a non-sensitive preference and supports English and Hindi.

Farmers retrieve their own payments from `GET /api/payments/my-history`; the backend scopes this query to the authenticated account. Farmer procurement history, notifications, password reset, OTP resend, and some profile operations are not available in the current API. Staff analytics use records returned by the API; no sample records or predicted wait times are generated.

Frontend route and role checks are for navigation only. Spring Security remains the authority for access control.

The backend recognizes the `ADMIN` role, but a dedicated admin-management interface and secure provisioning workflow are not part of this frontend. Procurement centers do not currently have stored coordinates, and location lookup is not available until a geocoding provider is configured.
