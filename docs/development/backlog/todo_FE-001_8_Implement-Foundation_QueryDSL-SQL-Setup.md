# FE-001 8 — Implement Foundation: QueryDSL SQL setup

Feature ID: FE-001
Priority: 8
Owner: Backend
Status: todo

Objective
- Configure QueryDSL SQL (no JPA).
- Provide SQLQueryFactory bean; generate Q-classes via SQL codegen.

Files to add
- backend/web/src/main/java/web/common/querydsl/QuerydslConfig.java
- Configure codegen in pom via exec-maven-plugin (dev profile)
- Ensure target/generated-sources/querydsl is added as a source root

Acceptance criteria
- Q* classes generated for users, country, province.
- Repository compiles against SQLQueryFactory and Q classes.

Steps
1) Add QuerydslConfig with PostgreSQLTemplates and SQLQueryFactory.
2) Configure MetaDataExporter execution after schema is present (manual run in dev).
3) Verify Q classes and sample repository compile.

Test plan
- Compile after codegen; no missing types.
