# DDD Test Structure Documentation

## Overview

This document outlines the improved test structure that aligns with Domain-Driven Design (DDD) principles and bounded context boundaries. The test organization follows the same architectural patterns as the main source code, ensuring consistency and maintainability.

## Test Structure Principles

### 1. Bounded Context Alignment
- Tests are organized by bounded context (usermanagement, notification, analytics, security)
- Each bounded context has its own test package structure
- Cross-context integration is tested through event-driven scenarios

### 2. Layered Architecture Testing
- **Domain Layer Tests**: Unit tests for entities, value objects, and domain services
- **Application Layer Tests**: Integration tests for application services and event handlers
- **Infrastructure Layer Tests**: Repository and external integration tests
- **Presentation Layer Tests**: Controller and API integration tests

### 3. Test Categories

#### Unit Tests
- Fast, isolated tests for individual components
- Mock external dependencies
- Focus on business logic and domain rules
- Located in the same package structure as the source code

#### Integration Tests
- Test component interactions within a bounded context
- Use real database and Spring context
- Test event publishing and handling
- End-to-end scenarios within bounded context boundaries

#### Architecture Tests
- Enforce DDD architectural constraints
- Validate bounded context boundaries
- Ensure layered architecture compliance
- Prevent architectural drift

## Directory Structure

```
src/test/java/com/iqkv/sample/webmvc/dashboard/
├── BoundedContextArchitectureTest.java          # Architecture compliance tests
├── TechnicalStructureTest.java                 # Technical architecture tests
├── shared/
│   └── testutil/
│       ├── BoundedContextTestBase.java         # Base class for BC tests
│       └── DomainEventTestUtil.java            # Event testing utilities
├── usermanagement/                             # User Management BC tests
│   ├── domain/
│   │   ├── UserTest.java                       # User entity tests
│   │   ├── EmailTest.java                      # Email value object tests
│   │   └── UserDomainServiceTest.java          # Domain service tests
│   ├── application/
│   │   ├── UserManagementServiceTest.java      # Application service tests
│   │   └── UserEventHandlerTest.java           # Event handler tests
│   ├── infrastructure/
│   │   └── UserRepositoryIT.java               # Repository integration tests
│   └── presentation/
│       ├── UserManagementControllerIT.java     # Admin API tests
│       ├── AccountControllerIT.java            # Account API tests
│       └── PublicUserControllerIT.java         # Public API tests
├── notification/                               # Notification BC tests
│   ├── domain/
│   │   └── NotificationTest.java               # Notification entity tests
│   ├── application/
│   │   └── NotificationServiceIT.java          # Service integration tests
│   └── presentation/
│       └── NotificationControllerIT.java       # API integration tests
├── analytics/                                  # Analytics BC tests
│   ├── domain/
│   │   └── UserActivityTest.java               # Activity entity tests
│   ├── application/
│   │   └── AnalyticsServiceIT.java             # Service integration tests
│   └── presentation/
│       └── AnalyticsControllerIT.java          # API integration tests
└── security/                                   # Security BC tests
    ├── domain/
    │   └── SecurityEventTest.java              # Security event tests
    ├── application/
    │   └── SecurityServiceIT.java              # Service integration tests
    └── presentation/
        └── SecurityControllerIT.java           # API integration tests
```

## Test Naming Conventions

### Test Class Naming
- **Unit Tests**: `[ClassName]Test.java` (e.g., `UserTest.java`)
- **Integration Tests**: `[ClassName]IT.java` (e.g., `UserRepositoryIT.java`)
- **Architecture Tests**: `[Concept]ArchitectureTest.java`

### Test Method Naming
Use descriptive names that express the business scenario:
- `shouldCreateUserSuccessfully()`
- `shouldRejectUserCreationWithExistingEmail()`
- `shouldTrackUserRegistrationActivity()`
- `shouldSendWelcomeNotificationOnUserRegistration()`

## Test Categories and Annotations

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class UserTest {
    // Fast, isolated tests with mocks
}
```

### Integration Tests
```java
@SpringBootTest
@IntegrationTest
@Transactional
class UserManagementServiceIT {
    // Full Spring context with real database
}
```

### Repository Tests
```java
@DataJpaTest
@ContextConfiguration(classes = IntegrationTest.class)
class UserRepositoryIT {
    // JPA slice tests with embedded database
}
```

### Web Layer Tests
```java
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class UserManagementControllerIT {
    // Web layer tests with MockMvc
}
```

## Cross-Bounded Context Testing

### Event-Driven Integration
Test cross-context communication through domain events:

```java
@Test
void shouldCreateNotificationOnUserRegistration() {
    // Given
    UserRegisteredEvent event = new UserRegisteredEvent(...);
    
    // When
    eventPublisher.publishEvent(event);
    
    // Then
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
        // Verify notification was created
    });
}
```

### Eventual Consistency Testing
Use Awaitility for testing asynchronous operations:

```java
await()
    .atMost(Duration.ofSeconds(5))
    .untilAsserted(() -> {
        // Assertion that should eventually be true
    });
```

## Test Data Management

### Test Isolation
- Each test should be independent and not rely on other tests
- Use `@Transactional` for automatic rollback
- Clean up test data in `@AfterEach` methods
- Clear caches between tests

### Test Data Builders
Create builder patterns for complex test data:

```java
public class UserTestDataBuilder {
    public static User createTestUser() {
        return new User()
            .setLogin("testuser")
            .setEmail("test@example.com")
            .setActivated(true);
    }
}
```

## Best Practices

### 1. Test Organization
- Group related tests in nested classes using `@Nested`
- Use descriptive test class and method names
- Follow the AAA pattern (Arrange, Act, Assert)

### 2. Bounded Context Isolation
- Test each bounded context independently
- Use events for cross-context integration testing
- Mock external bounded context dependencies in unit tests

### 3. Domain-Centric Testing
- Focus on testing business rules and domain logic
- Test domain events and their handlers
- Verify invariants and constraints

### 4. Performance Considerations
- Keep unit tests fast (< 100ms)
- Use `@DataJpaTest` for repository tests
- Mock external services and databases in unit tests
- Use test slices to reduce Spring context loading time

### 5. Maintainability
- Use shared test utilities for common operations
- Create base classes for common test setup
- Keep tests simple and focused on single scenarios
- Regularly refactor tests to maintain clarity

## Migration from Legacy Structure

### Steps to Migrate Existing Tests

1. **Identify Bounded Context**: Determine which bounded context each test belongs to
2. **Categorize by Layer**: Classify tests as domain, application, infrastructure, or presentation
3. **Move and Rename**: Relocate tests to appropriate packages and rename following conventions
4. **Update Dependencies**: Adjust imports and test configurations
5. **Enhance with DDD Patterns**: Add event testing and cross-context scenarios
6. **Validate Architecture**: Run architecture tests to ensure compliance

### Legacy Test Mapping

| Legacy Location | New Location | Test Type |
|----------------|--------------|-----------|
| `web/rest/UserResourceIT` | `usermanagement/presentation/UserManagementControllerIT` | Integration |
| `service/UserServiceIT` | `usermanagement/application/UserManagementServiceIT` | Integration |
| `domain/UserTest` | `usermanagement/domain/UserTest` | Unit |
| `repository/UserRepositoryTest` | `usermanagement/infrastructure/UserRepositoryIT` | Integration |

## Tools and Dependencies

### Required Dependencies
```xml
<!-- Testing Framework -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Architecture Testing -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <scope>test</scope>
</dependency>

<!-- Async Testing -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

### IDE Configuration
- Configure test runners to recognize the new structure
- Set up code coverage to report by bounded context
- Create run configurations for different test categories

## Continuous Integration

### Test Execution Strategy
1. **Fast Feedback Loop**: Run unit tests first
2. **Integration Testing**: Run bounded context integration tests in parallel
3. **Architecture Validation**: Run architecture tests to catch violations
4. **Cross-Context Testing**: Run full integration tests last

### Coverage Requirements
- Domain layer: 90%+ coverage
- Application layer: 85%+ coverage
- Infrastructure layer: 70%+ coverage
- Presentation layer: 80%+ coverage

This improved test structure ensures that your tests are aligned with DDD principles, maintainable, and provide comprehensive coverage of your bounded contexts while maintaining clear separation of concerns.