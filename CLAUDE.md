# Nifilili — Development Rules

Spring Boot 3.5.7, Java 21, Spring Modulith 1.2.2, PostgreSQL, Flyway, JWT, MapStruct 1.6.3, Lombok, SpringDoc OpenAPI 2.8.5. Build: `mvn clean compile`. Tests: `mvn test`. Run: `mvn spring-boot:run`.

## Architecture
- Modules: core, auth, business, job, offering, order, quote, kyc, admin, messaging, payment, review, subscription, support
- Cross-module communication via `ApplicationEventPublisher` only — never inject services across modules
- All entities extend `BaseEntity` (time-based IDs via `IdGeneratorContext.generate()`)
- Controllers split by role: `controller/admin/`, `controller/owner/`, `controller/publicapi/`
- Module packages: `controller/`, `service/impl/`, `repository/`, `domain/`, `dto/request/`, `dto/response/`, `mapper/`, `events/`, `validation/`

## Code Standards
- Lombok: `@RequiredArgsConstructor` on services/controllers, `@Slf4j` for logging, `@Builder`+`@Getter` on entities/response DTOs, `@Data` on request DTOs
- Naming: PascalCase classes, camelCase methods, UPPER_SNAKE constants, snake_case DB columns/tables
- 4-space indent, 120-char line limit, K&R braces, no wildcard imports in production code
- Imports: java → jakarta → third-party → spring → com.nifilili

## Controllers
- Class: `@RestController`, `@RequestMapping("/api/v1/<module>")`, `@PreAuthorize`, `@Tag(name="<Domain> <Purpose> [<Role>]", description)`
- Methods: `@Operation(summary, description)`, `@ApiResponse` for each status code (200/201/400/401/403/404/409), `@Valid` on `@RequestBody`, return `ResponseEntity<T>`
- No business logic — delegate to services

## Services
- Interface + Impl pattern. `@Service`, `@Transactional` for mutations, `@Transactional(readOnly=true)` for queries
- Use `SecurityUtil.getCurrentUserId()` for auth context. Throw exceptions from `core.exception`
- Javadoc on all public interface methods (purpose, @param, @return, @throws)

## Exceptions
- Custom exceptions in `core.exception/`, handler in `GlobalExceptionHandler`, response via `ErrorDto(status, message)`
- `log.error` for 5xx, `log.warn` for 4xx. Never expose stack traces to clients

## Logging
- `@Slf4j` only. Parameterized `{}` placeholders — no concatenation. Never log passwords/tokens/PII
- Controllers: `log.info` entry/exit. Services: `log.debug` flow, `log.info` state changes

## Database
- Flyway migrations in `src/main/resources/db/migration/` named `V<N>__<snake_case>.sql`
- Never modify existing migrations. DDL-auto=validate — all schema via Flyway only

## Testing (mandatory every change)
- JUnit 5 + Mockito. `@ExtendWith(MockitoExtension.class)`. Test naming: `method_WhenCondition_ShouldBehavior`
- Use `SecurityContextTestUtil` (set/clear auth) and `TestEntityIdUtil` (deterministic IDs) from `src/test/java/com/nifilili/business/`
- Minimum: 1 happy-path + 1 failure test per new/modified public method. Assert return values, `ArgumentCaptor` for saves, event payloads

## Postman
- Collections in `src/test/resources/<module>/e2e/postman/`. Update on every endpoint change
- Use env variables (`{{base_url}}`, `{{token}}`), assert status codes and response shape
- Newman scripts in `src/test/resources/<module>/e2e/scripts/`

## Comments
- Comment "why" not "what". No redundant comments. Javadoc on service interface methods. `// TODO(<author>): description`

## Pre-Commit Checklist
Compile passes → tests green → unit tests written → Swagger annotations present → Postman updated → no secrets → Flyway migration if schema changed → exceptions handled in GlobalExceptionHandler → logging present, no PII
