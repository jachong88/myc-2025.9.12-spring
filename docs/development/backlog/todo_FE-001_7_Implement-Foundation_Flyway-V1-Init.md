# FE-001 7 — Implement Foundation: Flyway V1 init

Feature ID: FE-001
Priority: 7
Owner: Backend
Status: todo

Objective
- Create baseline schema for users, country, province per data dictionaries.
- Soft-delete fields and partial unique indexes on active rows.

Files to add
- backend/web/src/main/resources/db/migration/V1__init.sql

Acceptance criteria
- Flyway migrates successfully on startup.
- Tables:
  - users (ULID PK, audit, soft-delete, partial uniques on email/phone)
  - country (ULID PK, ISO codes)
  - province (ULID PK, country_id logical ref, ISO 3166-2 code)

Steps
1) Write V1__init.sql using the provided data dictionaries.
2) Add helpful indexes for country_id/province_id/role_id.

Test plan
- Start app; verify migration applies with no errors.
