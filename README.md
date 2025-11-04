# AGILE BANK — Full-Stack Banking Demo  
Spring Boot (Java 21) • React (Vite) • PostgreSQL • JWT + Sessions • Docker Compose • GitHub Actions

> **Screenshots**: All UI/API screenshots live under `docs/images/`.
> **Week-1 report**: see `docs/week1-report.md` (or your RTF) and `docs/week1-findings.md`.

---

## 1) Overview

**AGILE BANK** is a teaching/demo banking app with **authentication + authorization**, **persistent data** (users, accounts, transfers), and a **modern full-stack** (Spring Boot + React). It is intentionally “tight but not perfect” so security tooling and improvements are meaningful.

- **Why this project?**
  - Realistic flows: **sign-up, login, dashboard, accounts (checking/savings), profile, pay & transfer, password reset**.
  - Uses tech I know (Java/React).
  - Small enough to understand in a week.
  - Has security requirements (auth, sensitive data).
  - Integrates **security tools** in CI (Dependency-Check, Trivy, ZAP; optional CodeQL).

---

## 2) Tech Stack

- **Backend**: Java 21, Spring Boot 3.3.x, Spring Security, Spring Data JPA, Hibernate Validator, JJWT
- **Frontend**: React (Vite), vanilla CSS, Nginx container for prod build
- **DB**: PostgreSQL 16
- **Auth**: JWT (stateless) + HTTP Session (for demo UX)
- **CI/CD**: GitHub Actions (Java CI, Node CI, Security Scans, optional CodeQL, optional ZAP DAST)
- **Containers**: Docker Compose (frontend + backend + postgres)

---

## 3) Repository Layout

```
.
├─ backend/                    # Spring Boot application
│  ├─ src/main/java/com/example/bankapp
│  │  ├─ auth/                # AuthController (signup/login), password reset
│  │  ├─ security/            # SecurityConfig, JwtAuthFilter, JwtService, RoleInterceptor
│  │  ├─ user/                # User, Account entities; Repos; UserService; UserController
│  │  ├─ transfer/            # TransferService + TransferController
│  │  ├─ config/              # DataSeeder (admin user), WebConfig/CORS if needed
│  │  └─ BankAppApplication   # Spring Boot main
│  └─ pom.xml
│
├─ frontend/                  # React (Vite)
│  ├─ src/
│  │  ├─ components/          # shared components
│  │  ├─ pages/               # Login, Signup, Dashboard, Accounts, Profile, Transfer, Help, etc.
│  │  ├─ App.jsx
│  │  └─ main.jsx
│  ├─ index.html
│  ├─ package.json
│  └─ nginx.conf              # prod container reverse proxy (/api → backend)
│
├─ .github/workflows/         # CI workflows (Java, Node, Security, CodeQL, ZAP)
├─ docs/                      # Reports & screenshots (images under docs/images/)
├─ docker-compose.yml         # Build & run all services
└─ README.md
```

---

## 4) Quick Start (Docker Compose)

> **Prereq**: Docker Desktop (or Docker Engine + Compose).

From the repo root:

```bash
docker compose down -v          # clean start (removes volumes)
docker compose up --build -d    # build & start all containers
docker compose ps               # verify all are up
```

**URLs**

- Frontend (Nginx serving built React): `http://localhost:5173/`
- Backend health: `http://localhost:8080/actuator/health`
- Postgres: `localhost:5432` (internal use via Compose network)

**Seeded Admin user** (from DataSeeder):

```
email:    admin@bank.local
password: Password123!
```

---

## 5) Local Development (without Docker)

### Backend (Spring Boot)

```bash
cd backend
# If using local Postgres, export envs (adjust as needed):
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bankdb
export SPRING_DATASOURCE_USERNAME=bankuser
export SPRING_DATASOURCE_PASSWORD=bankpass
export SPRING_JPA_HIBERNATE_DDL_AUTO=update
# If your app reads this:
export APP_JWT_SECRET=dev-local-secret

# Run tests + build
mvn -B -ntp clean verify

# Run app
mvn -B -ntp spring-boot:run
```

### Frontend (Vite dev server)

```bash
cd frontend
npm ci
npm run dev
# Vite dev server -> http://localhost:5173
```

**Dev API routing**  
The frontend calls the API with **relative `/api/...`**. In Docker, Nginx proxies `/api` → backend. In Vite dev, you can add a proxy in `vite.config.js` if needed, or start the backend on `8080` and rely on same-host CORS settings.

---

## 6) Using the App

### 6.1 Sign up a new user

**UI**: use the **Signup** page (validates unique email and 7-digit SSN).  
**API**:

```bash
curl -i -X POST http://localhost:8080/api/auth/signup   -H "Content-Type: application/json"   -d '{
    "email":"new.user1@bank.local",
    "password":"Password123!",
    "firstName":"New",
    "lastName":"User",
    "address":"100 Main St, Boston, MA",
    "phone":"555-1000",
    "ssn7":"1234567"
  }'
```

### 6.2 Login (JWT + Session)

```bash
curl -i -X POST http://localhost:8080/api/auth/login   -H "Content-Type: application/json"   -d '{"email":"admin@bank.local","password":"Password123!"}'
```

Returns JSON with `"token": "<jwt>"` and a session cookie.  
In the UI, after login you’ll see **Dashboard** with user/account overview.

### 6.3 Authenticated endpoints (examples)

```bash
# Get current user profile
curl -s http://localhost:8080/api/users/me   -H "Authorization: Bearer <PUT_JWT_HERE>"

# Get my accounts (checking & savings)
curl -s http://localhost:8080/api/accounts/me   -H "Authorization: Bearer <PUT_JWT_HERE>"
```

### 6.4 Password reset flow

```bash
# Request a reset token (demo returns the token)
curl -s -X POST http://localhost:8080/api/auth/forgot-password   -H "Content-Type: application/json"   -d '{"email":"new.user1@bank.local"}'

# Reset using token from previous response
curl -s -X POST http://localhost:8080/api/auth/reset-password   -H "Content-Type: application/json"   -d '{"token":"<RESET_TOKEN>","newPassword":"NewStrongPass!234"}'
```

---

## 7) Frontend Pages (Bank-like Navigation)

- **Login / Signup / Forgot / Reset** – Auth flows (JWT + session).
- **Dashboard** – User summary + account overview.
- **Checking / Savings** – Account lists and balances.
- **Profile & Settings** – Email, name, address, phone.
- **Pay & Transfer** – Intra-user transfers with server checks.
- **Security Center** – Placeholder for advanced features (CSP, device mgmt, 2FA).
- **Help & Support** – Project info and links.

---

## 8) Branching Model (develop → main)

1. Create `develop` from `main` (once):
   ```bash
   git checkout main && git pull
   git checkout -b develop
   git push -u origin develop
   ```

2. Do work on `develop` (or feature branches → PR into `develop`).

3. When ready to “release”, open a PR **develop → main**.  
   Protect `main` in GitHub (Settings → Branches → Protection rules) to require PRs and passing checks.

4. (Optional) Tag a release after merge:
   ```bash
   git checkout main && git pull
   git tag -a v1.0.0 -m "AGILE BANK v1.0.0"
   git push origin v1.0.0
   ```

**Run CI on both branches**: in each workflow, set  
`branches: [ main, develop ]` for `push`/`pull_request`.

---

## 9) CI / GitHub Actions

Workflows in `.github/workflows/`:

- **Backend CI (Java)** — builds & tests Spring Boot, packages the JAR.  
  Uses a **Postgres service** in CI so tests can run:
  - `java.yml`

- **Frontend CI (Node)** — installs and builds React (Vite).  
  - `node.yml`

- **Security Scans** — OWASP Dependency-Check (SCA) and Trivy (filesystem)  
  - `security.yml`  
  Artifacts: HTML reports for Dependency-Check.

- **CodeQL (optional)** — static analysis for Java & JavaScript  
  - `codeql.yml`  
  Results: **Security → Code scanning alerts**.

- **DAST - OWASP ZAP Baseline (optional)** — spins up stack via Compose, scans frontend & API, uploads HTML reports  
  - `zap-dast.yml`  
  Artifacts: `zap-frontend.html`, `zap-backend.html`.

**Tips**
- If backend tests don’t need DB: switch to H2 test profile (faster).
- If you want CI to **fail** on High/Critical vulns, set stricter exit codes in Trivy/ZAP steps.

---

## 10) Security Features (Current)

- **Authentication & Session**
  - **BCrypt** password hashing
  - **JWT** issuance/validation (JJWT); auth filter populates SecurityContext
  - Demo HTTP session for UX (consider Secure/SameSite in prod)
  - Password reset tokens (short-lived, stored in DB)

- **Authorization**
  - `/api/auth/**` and `/actuator/health` are public
  - All other routes require authentication
  - Ownership checks (users only access their data/accounts)

- **Data Protection**
  - PII in Postgres (dev); passwords hashed
  - TLS recommended in production
  - JWT secret should come from environment/secret manager in prod

- **Headers & Validation**
  - Spring Security default headers (no-sniff, frame options, cache control)
  - Hibernate Validator for request bodies (unique email, SSN 7-digit, etc.)

**Planned hardening**
- Shorter JWT TTL + **iss/aud** checks; key via env/secret
- Rate limiting (`/api/auth/login`, reset)
- Strict **CORS** / CSP / HSTS / Referrer-Policy / Permissions-Policy
- Audit logging; admin role; secrets management
- ZAP **active** scans in CI

---

## 11) Testing

### Backend (unit/integration)
```bash
cd backend
mvn -B -ntp clean verify
```

### Frontend (if you add tests)
```bash
cd frontend
npm test -- --ci
```

---

## 12) Troubleshooting

**Port 5432 “already allocated”**
Another local Postgres is running. Stop it or change the mapped port in `docker-compose.yml`, e.g.:
```yaml
ports:
  - "15432:5432"
```
Then update `SPRING_DATASOURCE_URL` accordingly.

**Backend container exits immediately**
Run logs:
```bash
docker compose logs backend --tail=200
```
Typical causes:
- DB not reachable → ensure `postgres` container is healthy.
- Bean cycle in security config → ensure `JwtAuthFilter` is injected into the filter chain but **not** circularly referencing `SecurityConfig`.

**“database X does not exist”**
Ensure `POSTGRES_DB` in Compose matches the JDBC URL DB name and that the DB is created at init (Compose handles this by env).

**CORS/401 in browser**
Use **relative `/api`** and ensure Nginx proxy in `frontend/nginx.conf`:
```nginx
location /api/ {
  proxy_pass http://backend:8080/api/;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
}
```

**CodeQL/Java build failing**
Give CodeQL a Postgres service (see `codeql.yml` in this repo), or use an H2 test profile that doesn’t require Postgres to start.

---

## 13) Contributing / Dev Flow

1. Work on `develop` (or feature branches → PR into `develop`).
2. Keep commits small and messages clear.
3. Run `docker compose up --build -d` locally to verify.
4. Submit PRs:
   - **feature → develop** (iteration)
   - **develop → main** (release)

---

## 14) License & Credits

This project is for educational/demo purposes.
Security tooling used:
- OWASP **Dependency-Check**
- Aqua Security **Trivy**
- OWASP **ZAP**
- (Optional) GitHub **CodeQL**

---

## 15) Appendix — Handy Commands

**Compose lifecycle**

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f backend
docker compose down -v
```

**Seeded admin login**

```bash
curl -i -X POST http://localhost:8080/api/auth/login   -H "Content-Type: application/json"   -d '{"email":"admin@bank.local","password":"Password123!"}'
```

**Create a develop branch + PR to main**

```bash
git checkout main && git pull
git checkout -b develop
git push -u origin develop
# open PR: develop → main (on GitHub)
```

Author: Battal Cevik | 2025


