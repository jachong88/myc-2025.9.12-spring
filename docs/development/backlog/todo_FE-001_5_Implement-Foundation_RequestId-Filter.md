# FE-001 5 — Implement Foundation: RequestId filter

Feature ID: FE-001
Priority: 5
Owner: Backend
Status: todo

Objective
- Add RequestIdFilter to capture/assign ULID from X-Request-Id or generate a new one.
- Store in MDC for logging; expose via RequestIdHolder.

Files to add
- backend/web/src/main/java/web/common/request/RequestIdFilter.java
- backend/web/src/main/java/web/common/request/RequestIdHolder.java

Acceptance criteria
- Each response includes the same requestId in the envelope.
- X-Request-Id is honored if valid ULID.

Steps
1) Implement OncePerRequestFilter to manage requestId in MDC.
2) Replace temporary ULID generation in ApiExceptionHandler with RequestIdHolder.get().

Test plan
- Manual test: send requests with/without X-Request-Id; verify envelope’s requestId.
