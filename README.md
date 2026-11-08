# KisanMitra

KisanMitra is a farmer procurement and queue-management application. Farmers can find procurement centers, review available slots, book a slot, and follow their queue. Center operators and administrators can manage procurement operations and review service data.

## Project layout

- `FrontEnd/Vite-Project` — React 19, Vite, React Router, Tailwind CSS, and i18next frontend.
- `Backend/backend` — Java 21 and Spring Boot REST API using Spring Security, JPA, and MySQL.
- `AI-Services` — supporting AI service code.
- `Docs/Requirements.md` — project requirements.

## Prerequisites

- Node.js and npm
- Java 21+
- MySQL
- SMTP credentials for email verification during registration

## Run locally

### Backend

Create a MySQL database named `smart_farmer_db`. From `Backend/backend`, create a local `.env` file with:

```dotenv
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
MAIL_USERNAME=your_email_address
MAIL_PASSWORD=your_email_app_password
JWT_SECRET=your_long_random_secret
```

`JWT_SECRET` must contain at least 32 bytes of high-entropy text. Keep real credentials out of source control. From `Backend/backend`, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8080`. Email delivery must be configured for registration verification. The backend uses the configured datasource and updates the schema through JPA.

### Frontend

In a second terminal, from `FrontEnd/Vite-Project`, run:

```powershell
npm install
npm run dev
```

Open the URL Vite prints (usually `http://localhost:5173`). In development, Vite proxies `/api` requests to `http://localhost:8080`.

For a deployed frontend, copy `.env.example` to `.env` and set `VITE_API_BASE_URL` to the API origin. This is public client configuration; never put secrets in frontend variables. Configure backend CORS and cookies for the deployed HTTPS origins.

## First administrator provisioning

Public registration creates farmer accounts only. To provision the first administrator, configure the backend `.env`, use a native interactive terminal, and run from `Backend/backend`:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\backend-0.0.1-SNAPSHOT.jar --spring.main.web-application-type=none --app.provision-admin=true
```

The command prompts for user details and a hidden, confirmed password (minimum 12 characters). The password is BCrypt-hashed. A MySQL advisory lock prevents concurrent bootstrap; provisioning is refused if an administrator already exists. It is not an HTTP endpoint and refuses web-server mode. Do not pass passwords in command arguments or store them in source control.

## Validation

From `FrontEnd/Vite-Project`:

```powershell
npm run lint
npm run build
```

From `Backend/backend`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## Current feature boundaries

- Role checks are enforced by Spring Security; frontend role-aware routes only control navigation.
- Farmers can view their own payment records through `GET /api/payments/my-history`; ownership comes from the authenticated account and existing payment/procurement/token/farmer relations.
- Farmer-scoped procurement history and notifications are not exposed by the current API. Password reset and OTP resend are also unavailable.
- A dedicated admin dashboard, user management, operator-to-center assignments, and audit-log APIs are not implemented.
- Procurement centers currently lack stored latitude/longitude coordinates. GPS-based nearby sorting and reverse-geocoded profile details require configured, tested location integration; do not infer coordinates.
- Slot booking and queue operations use existing backend APIs, but have not all been comprehensively integration-tested.
- There is no configured AI prediction or weather-data integration. The app should not present generated estimates as authoritative records.

No sample centers, payment records, GPS coordinates, or credentials are provided by this project.
