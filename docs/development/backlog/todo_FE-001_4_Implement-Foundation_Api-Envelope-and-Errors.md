# FE-001 4 — Implement Foundation: API envelope and error handling

Feature ID: FE-001
Priority: 4
Owner: Backend
Status: todo

Objective
- Implement API envelope per docs/common/api/api_response_spec.md.
- Implement ErrorCode, AppException, ApiExceptionHandler.

Files to add
- backend/web/src/main/java/web/common/dto/ApiError.java
- backend/web/src/main/java/web/common/dto/ApiResponse.java
- backend/web/src/main/java/web/common/exception/ErrorCode.java
- backend/web/src/main/java/web/common/exception/AppException.java
- backend/web/src/main/java/web/common/exception/ApiExceptionHandler.java
- backend/web/src/main/java/web/common/util/Ulids.java (optional helper)

Acceptance criteria
- AppException returns 4xx/5xx envelope:
  {
    "success": false,
    "requestId": "<ulid>",
    "data": null,
    "meta": null,
    "error": { "status": <int>, "code": "<CODE>", "message": "<localized>", "details": {...}? }
  }

Steps
1) Add DTOs and exception classes.
2) Implement handler with i18n messages and error mappings.
3) Temporary: generate ULID in handler (replaced in Task 5 by RequestIdFilter).

Test plan
- Unit test validation error mapping.
