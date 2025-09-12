# FE-001 6 — Implement Foundation: Firebase security

Feature ID: FE-001
Priority: 6
Owner: Backend
Status: todo

Objective
- Firebase-only authentication (no /login/refresh/logout).
- Verify Firebase ID tokens via firebase-admin.
- Stateless Spring Security.

Files to add
- backend/web/src/main/java/web/common/security/FirebaseAuthFilter.java
- backend/web/src/main/java/web/common/security/SecurityConfig.java
- backend/web/src/main/java/web/common/security/CurrentUser.java (optional)
- backend/web/src/main/java/web/common/security/FirebaseAdminConfig.java (bootstrap)

Acceptance criteria
- Requests without token → 401 envelope.
- Valid token for existing user → 200 on protected routes.
- GET /api/v1/health, /actuator/**, /error → permitAll.

Steps
1) Configure firebase-admin using GOOGLE_APPLICATION_CREDENTIALS env var.
2) Implement filter: extract Bearer token, verify, resolve email, set Authentication.
3) Configure SecurityFilterChain: stateless, CSRF disabled, matchers per spec.

Test plan
- Manual: protected endpoint returns 401 then 200 with a valid token (or stub for dev).
