# FE-001 9 — Implement Foundation: Health endpoint

Feature ID: FE-001
Priority: 9
Owner: Backend
Status: todo

Objective
- Add GET /api/v1/health that returns the standard envelope with data: { "status": "ok" }.
- Keep actuator health as well.

Files to add
- backend/web/src/main/java/web/health/HealthController.java

Acceptance criteria
- GET /api/v1/health → success envelope { data: { status: "ok" } }.
- GET /actuator/health → UP.

Steps
1) Implement HealthController with the agreed envelope.
2) Verify both endpoints.

Test plan
- Manual GET both endpoints.
