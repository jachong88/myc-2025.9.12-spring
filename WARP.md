# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Repository scope: backend/web (Spring Boot, Maven)

1) Day-to-day commands (Windows PowerShell)
- Build (compile + unit tests)
  .\mvnw.cmd clean verify
  Notes:
  - verify runs unit tests (Surefire) and any integration tests matching *IT.java (Failsafe).
  - To skip tests: .\mvnw.cmd clean verify -DskipTests

- Run the app (dev)
  .\mvnw.cmd spring-boot:run

- Run unit tests only
  .\mvnw.cmd -DskipITs test
  (Failsafe integration tests only run during verify with *IT.java pattern.)

- Run a single test class
  .\mvnw.cmd -Dtest=WebApplicationTests test

- Run a single test method
  .\mvnw.cmd -Dtest=WebApplicationTests#contextLoads test

- Integration tests explicitly
  .\mvnw.cmd -DskipTests=false verify
  Pattern: files named *IT.java are executed by Failsafe during verify.
  Testcontainers requires Docker to be running.

- Package as JAR
  .\mvnw.cmd clean package
  Run the jar:
  java -jar target\web-0.0.1-SNAPSHOT.jar

- Lint/format
  No lint/format plugins are configured in pom.xml (e.g., Checkstyle/Spotless). If you need linting, add the appropriate Maven plugins first.

- QueryDSL SQL codegen (profile stub)
  Profile: querydsl-codegen
  .\mvnw.cmd -Pquerydsl-codegen generate-sources
  Notes:
  - The configured mainClass (web.codegen.QuerydslExporter) does not exist yet (commented as a later milestone). Do not run until implemented.
  - Intended env inputs: DB_URL, DB_USERNAME, DB_PASSWORD.

2) High-level architecture and structure
- Application entrypoint
  - com.web.WebApplication launches Spring Boot (Spring Boot 3, Java 21).

- Web/API
  - Spring MVC via spring-boot-starter-web.
  - CORS: web.common.config.WebConfig allows http://localhost:5173 with common methods and credentials for local frontend development.

- Security
  - spring-boot-starter-security is present; no custom config is in this module yet.

- Validation
  - spring-boot-starter-validation is present for bean validation.

- Persistence and migrations
  - spring-boot-starter-jdbc (no JPA).
  - PostgreSQL driver as runtime dependency.
  - Flyway enabled (application.yml): classpath:db/migration, baseline-on-migrate: true.
  - Default dev datasource (application.yml):
    jdbc:postgresql://localhost:5432/myc
    username: postgres
    password: ${DB_PASSWORD:postgres}
    Adjust DB_PASSWORD env var as needed.

- OpenAPI
  - springdoc-openapi-starter-webmvc-ui.
  - web.common.config.OpenApiConfig defines basic Info (title “MYC Web API”, v1).
  - Swagger UI will be available at /swagger-ui.html when running.

- IDs
  - ULID generation via ulid-creator dependency (no concrete usage shown yet in this module).

- Test strategy
  - Unit tests: JUnit 5 (maven-surefire-plugin defaults), e.g., com.web.WebApplicationTests.
  - Integration tests: maven-failsafe-plugin runs files matching *IT.java during verify.
  - Testcontainers:
    - Test configuration (com.web.TestcontainersConfiguration) supplies a PostgreSQLContainer bean annotated with @ServiceConnection so Spring can auto-wire DataSource to the container.
    - Requires local Docker to be running for integration tests.
    - Test app bootstrap: com.web.TestWebApplication uses SpringApplication.from(...).with(TestcontainersConfiguration.class).

- Configuration classes (common)
  - web.common.config.JacksonConfig: placeholder (defaults are fine for now).
  - web.common.config.WebConfig: CORS setup.
  - web.common.config.OpenApiConfig: OpenAPI bean.

- Packaging layout notes
  - The main application package is com.web, while supplementary config classes live under web.common.config (top-level “web” package). Keep package scanning in mind; Spring Boot scans from com.web by default due to @SpringBootApplication on com.web. If moving beans under other roots, ensure component scanning picks them up.

3) Development workflow notes (from docs)
- The docs/development/development-workflow-backend.md defines a feature/task loop driven by markdown files under docs/development/backlog:
  - Create todo_*.md task files, pick next task, implement with tests, then rename the file from todo_* to done_* and commit using the task file name in the message.
  - Refer to that document for file naming conventions and exact steps.
