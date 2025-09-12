# FE-001 2 — Implement Foundation: application.yml and core configs

Feature ID: FE-001
Priority: 2
Owner: Backend
Status: todo

Objective
- Add application.yml (dev) with DB and Flyway config.
- Add Jackson, Web, and OpenAPI configs.

Files to add
- backend/web/src/main/resources/application.yml
- backend/web/src/main/java/web/common/config/JacksonConfig.java
- backend/web/src/main/java/web/common/config/WebConfig.java
- backend/web/src/main/java/web/common/config/OpenApiConfig.java

Acceptance criteria
- App starts and exposes /actuator/health (UP).
- Swagger UI available at /swagger-ui/index.html.

application.yml (dev) — draft
```yaml
server:
  port: 8080

spring:
  application:
    name: web
  jackson:
    time-zone: UTC
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false
  datasource:
    url: jdbc:postgresql://localhost:5432/myc
    username: postgres
    password: ${DB_PASSWORD:postgres}
  flyway:
    enabled: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Steps
1) Add application.yml (above).
2) Add minimal JacksonConfig, WebConfig (CORS for dev), OpenApiConfig.
3) Start app and verify endpoints.

Test plan
- GET /actuator/health → UP.
- Open /swagger-ui/index.html → UI loads.
