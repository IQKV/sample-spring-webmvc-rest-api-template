# CLAUDE - Coding Guidelines and Best Practices

This document outlines the coding guidelines, best practices, and conventions for Java 21+ and Spring Boot 3 development in this project.

## Table of Contents

- [Java 21+ Features](#java-21-features)
- [Spring Boot 3 Best Practices](#spring-boot-3-best-practices)
- [Code Organization](#code-organization)
- [Coding Style](#coding-style)
- [Testing Strategies](#testing-strategies)
- [Security Guidelines](#security-guidelines)
- [Performance Considerations](#performance-considerations)
- [Documentation](#documentation)
- [Tooling](#tooling)
- [Contribution Workflow](#contribution-workflow)

## Java 21+ Features

### Virtual Threads

Use virtual threads for I/O-bound operations to improve scalability:
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
// Submit tasks to the executor
Future<?> future = executor.submit(() -> performIOOperation());
}
```
### Pattern Matching

Leverage pattern matching for `instanceof` and switch expressions:
```java
// Pattern matching for instanceof
if (obj instanceof String s) {
// Can use 's' directly here
}

// Pattern matching for switch
return switch (obj) {
case String s -> s.length();
case List<?> l -> l.size();
case null -> 0;
default -> -1;
};
```
### Records

Use records for immutable data carriers:
```java
public record UserDTO(String id, String username, String email) {}
```
### Text Blocks

Use text blocks for multi-line strings:
```java
String query = """
SELECT u.id, u.username, u.email
FROM users u
WHERE u.active = true
ORDER BY u.username
""";
```
### Sealed Classes

Use sealed classes to restrict class hierarchies:
```
java
public sealed interface Shape
permits Circle, Rectangle, Triangle {
double area();
}
```
### Enhanced Switch

Use enhanced switch expressions for cleaner code:
```java
DayOfWeek day = // ...
String typeOfDay = switch (day) {
case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
case SATURDAY, SUNDAY -> "Weekend";
};
```
## Spring Boot 3 Best Practices

### Use Jakarta EE APIs

Spring Boot 3 uses Jakarta EE, not the legacy `javax` packages:
```java
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
```
### Prefer Constructor Injection

Use constructor injection for dependencies:
```
java
@Service
public class UserService {
private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```
For multiple dependencies, consider using `@RequiredArgsConstructor` from Lombok:
```java
@Service
@RequiredArgsConstructor
public class UserService {
private final UserRepository userRepository;
private final EmailService emailService;
}
```
### Use Spring Boot Configuration Properties

Group related properties together with `@ConfigurationProperties`:
```java
@ConfigurationProperties(prefix = "app.security")
@ConstructorBinding
public record SecurityProperties(
long tokenValidityInSeconds,
String[] allowedOrigins,
boolean requireSsl
) {}
```
### Utilize Spring Boot Actuator

Enable relevant Actuator endpoints for monitoring:
```yaml
management:
endpoints:
web:
exposure:
include: health,info,metrics,prometheus
endpoint:
health:
show-details: when_authorized
```
### Embrace Observability

Set up appropriate logging, metrics, and tracing:
```java
@RestController
@RequiredArgsConstructor
public class UserController {
private final UserService userService;
private final MeterRegistry meterRegistry;
private final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        Timer.Sample sample = Timer.start(meterRegistry);
        log.debug("Fetching all users");
        
        List<UserDTO> users = userService.getAllUsers();
        
        sample.stop(meterRegistry.timer("api.users.get"));
        log.debug("Fetched {} users", users.size());
        
        return users;
    }
}
```
## Code Organization

### Package Structure

Organize code by feature rather than layer:
```

com.iqkv.sample.webmvc.dashboard
├── user                 # User feature
│   ├── api              # Controllers, DTOs, mappers
│   ├── domain           # Entities, repositories
│   └── service          # Services
├── auth                 # Authentication feature
│   ├── api
│   ├── domain
│   └── service
├── common               # Shared components
│   ├── config
│   ├── error
│   └── util
└── DashboardApplication.java
```
### Use Spring Modulith (Optional)

Consider using Spring Modulith for explicit module boundaries:
```java
@SpringBootApplication
@EnableModules
public class DashboardApplication {
public static void main(String[] args) {
SpringApplication.run(DashboardApplication.class, args);
}
}
```
## Coding Style

### Use Consistent Naming

- Classes: `PascalCase` 
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

### Prefer Immutability

Make classes immutable when possible:
```java
// Prefer this:
public record UserDTO(String id, String name) {}

// Or with Lombok:
@Value
public class UserDTO {
String id;
String name;
}

// Over mutable structures:
@Data
public class UserDTO {
private String id;
private String name;
}
```
### Use Modern Collections

Use factory methods for collections:
```java
// Instead of:
List<String> names = new ArrayList<>();
names.add("Alice");
names.add("Bob");

// Use:
List<String> names = List.of("Alice", "Bob");
```
### Leverage Java Stream API

Use streams for collection operations:
```java
// Instead of:
List<UserDTO> activeUsers = new ArrayList<>();
for (User user : users) {
if (user.isActive()) {
activeUsers.add(mapper.toDto(user));
}
}

// Use:
List<UserDTO> activeUsers = users.stream()
.filter(User::isActive)
.map(mapper::toDto)
.toList();
```
## Testing Strategies

### Use JUnit 5

Leverage JUnit 5 features like nested tests:
```java
@SpringBootTest
class UserServiceTests {

    @Nested
    class GetUserById {
        @Test
        void shouldReturnUserWhenExists() {
            // ...
        }
        
        @Test
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // ...
        }
    }
}
```
### Separate Test Types

Categorize tests by type:

- Unit tests (fast, isolated)
- Integration tests (with Spring context)
- End-to-end tests (full application)

### Use TestContainers

Use TestContainers for integration tests with real dependencies:
```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void shouldSaveAndRetrieveUser() {
        // Test using real PostgreSQL
    }
}
```
### Test Data Management

Use test data factories to create consistent test data:
```
java
public class UserTestFactory {
public static User createValidUser() {
return User.builder()
.id(UUID.randomUUID().toString())
.username("testuser")
.email("test@example.com")
.build();
}
}
```
## Security Guidelines

### Follow OWASP Top 10

Stay aware of the [OWASP Top 10](https://owasp.org/www-project-top-ten/) vulnerabilities:

1. Broken Access Control
2. Cryptographic Failures
3. Injection
4. Insecure Design
5. Security Misconfiguration
6. Vulnerable Components
7. Auth Failures
8. Software/Data Integrity Failures
9. Logging/Monitoring Failures
10. SSRF

### Secure APIs

Use proper authentication and authorization:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.canAccessUser(authentication, #id)")
    public UserDTO getUser(@PathVariable String id) {
        // ...
    }
}
```
### Input Validation

Always validate input data:
```java
@PostMapping
public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userDTO) {
// UserCreateDTO has validation annotations
// ...
}
```
### Content Security Policy

Configure appropriate headers:
```yaml
server:
  http2:
    enabled: true
ssl:
  enabled: true
servlet:
  session:
   cookie:
     secure: true
     http-only: true
     same-site: strict
```
## Performance Considerations

### Use Caching

Apply caching where appropriate:
```java
@Service
public class UserService {
@Cacheable(cacheNames = "users", key = "#id")
public UserDTO getUserById(String id) {
// ...
}

    @CacheEvict(cacheNames = "users", key = "#id")
    public void updateUser(String id, UserUpdateDTO dto) {
        // ...
    }
    
    @CacheEvict(cacheNames = "users", allEntries = true)
    public void invalidateCache() {
        // ...
    }
}
```
### Pagination

Always paginate large result sets:
```java
@GetMapping
public Page<UserDTO> getUsers(
@RequestParam(defaultValue = "0") int page,
@RequestParam(defaultValue = "20") int size,
@RequestParam(defaultValue = "username") String sort
) {
return userService.getUsers(PageRequest.of(page, size, Sort.by(sort)));
}
```
### Avoid N+1 Queries

Use fetch joins or entity graphs to avoid N+1 query problems:
```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
@EntityGraph(attributePaths = {"roles", "preferences"})
Optional<User> findWithDetailById(String id);
}
```
## Documentation

### API Documentation

Use Springdoc OpenAPI for API documentation:
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their unique identifier"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public UserDTO getUser(
        @Parameter(description = "User ID") @PathVariable String id
    ) {
        // ...
    }
}
```
### Javadoc

Document public APIs with Javadoc:
```java
/**
* Service for managing user accounts.
  */
  @Service
  public class UserService {

  /**
    * Retrieves a user by their unique identifier.
    *
    * @param id the user ID
    * @return the user DTO
    * @throws UserNotFoundException if the user doesn't exist
      */
      public UserDTO getUserById(String id) {
      // ...
      }
      }
```
## Tooling

### Code Formatting

Use oxftm for consistent formatting.

### Static Analysis

Use tools like SonarQube, SpotBugs, or PMD to find potential issues:
```xml
<plugin>
<groupId>com.github.spotbugs</groupId>
<artifactId>spotbugs-maven-plugin</artifactId>
<version>4.8.3.0</version>
<executions>
<execution>
<goals>
<goal>check</goal>
</goals>
</execution>
</executions>
</plugin>
```
### Dependency Management

Regularly update dependencies and scan for vulnerabilities:
```shell
./mvnw versions:display-dependency-updates
./mvnw dependency-check:check
```
## Contribution Workflow

### Git Flow

Use a Git flow process:

1. Create feature branch from `develop`: `git checkout -b feature/add-user-search`
2. Make changes and commit with conventional commits
3. Push and create PR to `develop`
4. After review and tests, merge to `develop`
5. Periodically, merge `develop` to `main` for releases

### Conventional Commits

Follow conventional commit format:
```

feat: add user search functionality
fix: correct email validation in user registration
docs: update API documentation for user endpoints
test: add integration tests for user repository
refactor: simplify authentication logic
```
### Code Reviews

Checklist for code reviews:

- Does the code follow project conventions?
- Are there appropriate tests?
- Is there sufficient documentation?
- Are there any security concerns?
- Is error handling implemented?
- Could performance be improved?
