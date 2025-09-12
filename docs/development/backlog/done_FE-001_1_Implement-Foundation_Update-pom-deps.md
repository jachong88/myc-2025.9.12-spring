# FE-001 1 — Implement Foundation: Update pom.xml and dependencies

Feature ID: FE-001
Priority: 1
Owner: Backend
Status: todo

Objective
- Add baseline dependencies and plugins per design-decisions:
  - Spring Boot: web, security, validation, actuator, jdbc
  - PostgreSQL driver
  - Flyway
  - Firebase Admin SDK
  - Springdoc OpenAPI
  - ULID creator (optional)
  - Lombok (optional)
  - QueryDSL SQL (no JPA) + SQL codegen
  - Testcontainers (Postgres + JUnit Jupiter)
  - Surefire/Failsafe/Exec plugins
- Ensure Java 21 toolchain.

Files to change
- backend/web/pom.xml

Acceptance criteria
- mvn -q -DskipTests package succeeds.
- Java toolchain is 21 (source/target).
- Dependencies include QueryDSL SQL and Firebase Admin.
- Plugins configured for unit/integration tests (Surefire/Failsafe).

Steps
1) Add core dependencies:
   spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-validation,
   spring-boot-starter-actuator, spring-boot-starter-jdbc, postgresql (runtime), flyway-core,
   firebase-admin, springdoc-openapi-starter-webmvc-ui, ulid-creator (optional), lombok (optional).
2) Add QueryDSL SQL deps:
   querydsl-sql, querydsl-sql-codegen (and optionally querydsl-sql-spring).
3) Add test deps:
   spring-boot-starter-test, testcontainers, testcontainers-postgresql, junit-jupiter.
4) Configure plugins:
   spring-boot-maven-plugin, maven-compiler-plugin (release 21; -parameters),
   maven-surefire-plugin (unit), maven-failsafe-plugin (integration),
   exec-maven-plugin (for QueryDSL SQL codegen).
5) Validate with mvn -q -DskipTests package.

Test plan
- Build succeeds locally without tests.
