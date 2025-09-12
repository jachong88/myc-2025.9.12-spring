# FE-001 3 — Implement Foundation: i18n setup (English seed)

Feature ID: FE-001
Priority: 3
Owner: Backend
Status: todo

Objective
- Add i18n config using Accept-Language.
- Seed English bundle (messages.properties).
- Provide ErrorMessageResolver.

Files to add
- backend/web/src/main/java/web/common/config/I18nConfig.java
- backend/web/src/main/java/web/common/i18n/ErrorMessageResolver.java
- backend/web/src/main/resources/messages.properties

Acceptance criteria
- error.message localizes based on Accept-Language (fallback to English).

messages.properties (seed keys)
```properties
error.validation_failed=Validation failed
error.argument_invalid=Invalid argument
error.unauthenticated=You must login first
error.access_denied=You are not allowed to access this resource
error.resource_not_found={0} {1} not found
error.duplicate_resource=Duplicate resource
error.conflict=Conflict occurred
error.rate_limited=Too many requests, please retry later
error.internal=Unexpected server error
```

Steps
1) Implement I18nConfig: MessageSource + AcceptHeaderLocaleResolver(default=en).
2) Implement ErrorMessageResolver: resolve(key,args) and resolve(AppException).

Test plan
- Unit test ErrorMessageResolver default locale.
