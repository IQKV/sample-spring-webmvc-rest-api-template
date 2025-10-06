# Legacy Test Migration Guide

## Overview
This guide provides step-by-step instructions for migrating remaining legacy tests to the new DDD-aligned bounded context structure.

## Migration Status

### ✅ Completed Migrations

#### User Management Bounded Context
- ✅ `UserResourceIT` → `UserManagementControllerIT`
- ✅ `PublicUserResourceIT` → `PublicUserControllerIT`
- ✅ `AccountResourceIT` → `AccountControllerIT`
- ✅ `UserServiceIT` → Enhanced `UserManagementServiceTest`
- ✅ `AuthorityTest` → `usermanagement/domain/AuthorityTest`
- ✅ `UserTest` → Already in correct location
- ✅ `UserRepositoryTest` → `UserRepositoryIT`

#### Security Bounded Context
- ✅ `SecurityUtilsUnitTest` → `security/domain/SecurityUtilsTest`
- ✅ Created `SecurityServiceIT`

#### Notification Bounded Context
- ✅ Created `NotificationServiceIT`
- ✅ Created `NotificationControllerIT`
- ✅ `NotificationTest` → Already in correct location

#### Analytics Bounded Context
- ✅ Created `AnalyticsServiceIT`
- ✅ Created `AnalyticsControllerIT`

### 🔄 Pending Migrations

#### Security Bounded Context
- `DomainUserDetailsServiceIT` → `security/infrastructure/DomainUserDetailsServiceIT`
- `AuthenticationIntegrationTest` → `security/presentation/AuthenticationControllerIT`
- `TokenAuthenticationIT` → `security/infrastructure/TokenAuthenticationIT`
- `TokenAuthenticationSecurityMetersIT` → `security/application/TokenAuthenticationServiceIT`

#### Shared/Infrastructure Tests
- `MailServiceIT` → `shared/infrastructure/MailServiceIT`
- `UserMapperTest` → `usermanagement/application/mapper/UserMapperTest`

#### Configuration Tests (Keep in current location but enhance)
- `WebConfigurerTest` → Enhance with bounded context awareness
- `CRLFLogConverterTest` → Keep as infrastructure test
- `HibernateTimeZoneIT` → Keep as infrastructure test

#### Management Tests
- `SecurityMetersServiceTests` → `security/application/SecurityMetersServiceTest`

## Step-by-Step Migration Process

### 1. Identify Bounded Context
For each legacy test, determine which bounded context it belongs to:

```
Test File                          → Target Bounded Context
DomainUserDetailsServiceIT        → Security
AuthenticationIntegrationTest     → Security  
MailServiceIT                     → Shared (cross-cutting)
UserMapperTest                    → User Management
SecurityMetersServiceTests        → Security
```

### 2. Determine Layer
Classify the test by architectural layer:

```
Layer           → Package Structure
Domain          → {context}/domain/
Application     → {context}/application/
Infrastructure  → {context}/infrastructure/
Presentation    → {context}/presentation/
```

### 3. Migration Template

#### For Domain Tests
```java
package com.iqkv.sample.webmvc.dashboard.{context}.domain;

/**
 * Unit tests for {Entity} domain entity within the {Context} bounded context.
 * 
 * Tests the domain logic, validation rules, and business constraints.
 */
class {Entity}Test {
    // Domain-focused unit tests
}
```

#### For Application Tests
```java
package com.iqkv.sample.webmvc.dashboard.{context}.application;

/**
 * Integration tests for {Service} within the {Context} bounded context.
 * 
 * Tests application service orchestration, event handling,
 * and cross-layer integration.
 */
@SpringBootTest
@IntegrationTest
class {Service}IT extends BoundedContextTestBase {
    // Application service integration tests
}
```

#### For Infrastructure Tests
```java
package com.iqkv.sample.webmvc.dashboard.{context}.infrastructure;

/**
 * Integration tests for {Repository} within the {Context} bounded context.
 * 
 * Tests data access patterns, custom queries, and external integrations.
 */
@DataJpaTest
@ContextConfiguration(classes = IntegrationTest.class)
class {Repository}IT {
    // Infrastructure integration tests
}
```

#### For Presentation Tests
```java
package com.iqkv.sample.webmvc.dashboard.{context}.presentation;

/**
 * Integration tests for {Controller} within the {Context} bounded context.
 * 
 * Tests HTTP API behavior, request/response handling,
 * and presentation layer concerns.
 */
@AutoConfigureMockMvc
@IntegrationTest
class {Controller}IT extends BoundedContextTestBase {
    // Web layer integration tests
}
```

## Specific Migration Instructions

### Security Context Migrations

#### 1. DomainUserDetailsServiceIT
```bash
# Current: src/test/java/com/iqkv/sample/webmvc/dashboard/security/DomainUserDetailsServiceIT.java
# Target:  src/test/java/com/iqkv/sample/webmvc/dashboard/security/infrastructure/DomainUserDetailsServiceIT.java
```

**Changes needed:**
- Move to infrastructure package
- Extend `BoundedContextTestBase`
- Focus on user details loading and security context integration
- Add bounded context cleanup

#### 2. AuthenticationIntegrationTest
```bash
# Current: src/test/java/com/iqkv/sample/webmvc/dashboard/security/jwt/AuthenticationIntegrationTest.java
# Target:  src/test/java/com/iqkv/sample/webmvc/dashboard/security/presentation/AuthenticationControllerIT.java
```

**Changes needed:**
- Rename to follow controller naming convention
- Move to presentation package
- Focus on HTTP authentication endpoints
- Test JWT token generation and validation

#### 3. TokenAuthenticationIT
```bash
# Current: src/test/java/com/iqkv/sample/webmvc/dashboard/security/jwt/TokenAuthenticationIT.java
# Target:  src/test/java/com/iqkv/sample/webmvc/dashboard/security/infrastructure/TokenAuthenticationIT.java
```

**Changes needed:**
- Move to infrastructure package
- Focus on token validation and security filter chain
- Test JWT parsing and validation logic

### Shared Infrastructure Migrations

#### 1. MailServiceIT
```bash
# Current: src/test/java/com/iqkv/sample/webmvc/dashboard/service/MailServiceIT.java
# Target:  src/test/java/com/iqkv/sample/webmvc/dashboard/shared/infrastructure/MailServiceIT.java
```

**Changes needed:**
- Move to shared infrastructure package
- Test email sending functionality
- Mock external email providers
- Test template rendering

#### 2. UserMapperTest
```bash
# Current: src/test/java/com/iqkv/sample/webmvc/dashboard/service/mapper/UserMapperTest.java
# Target:  src/test/java/com/iqkv/sample/webmvc/dashboard/usermanagement/application/mapper/UserMapperTest.java
```

**Changes needed:**
- Move to user management application package
- Test DTO to entity mapping
- Test entity to DTO mapping
- Validate mapping completeness

## Migration Checklist

For each migrated test, ensure:

- [ ] **Package Structure**: Test is in correct bounded context package
- [ ] **Naming Convention**: Follows new naming patterns (Test vs IT suffix)
- [ ] **Base Class**: Extends appropriate base class if needed
- [ ] **Imports**: Updated to use bounded context classes
- [ ] **Documentation**: Updated JavaDoc with bounded context focus
- [ ] **Cleanup**: Implements proper test data cleanup
- [ ] **Assertions**: Uses domain-appropriate assertions
- [ ] **Dependencies**: Uses correct repositories and services

## Validation Steps

After migration:

1. **Run Tests**: Ensure all migrated tests pass
2. **Architecture Tests**: Verify no architecture violations
3. **Coverage**: Check test coverage is maintained
4. **Dependencies**: Validate no circular dependencies
5. **Performance**: Ensure test execution time is reasonable

## Legacy Test Cleanup

After successful migration:

1. **Delete Legacy Files**: Remove old test files
2. **Update References**: Fix any remaining references
3. **Clean Imports**: Remove unused import statements
4. **Update Documentation**: Update any test documentation

## Common Migration Patterns

### Pattern 1: Service Test Migration
```java
// Before (Legacy)
@IntegrationTest
class UserServiceIT {
    @Autowired UserService userService;
    // Generic service tests
}

// After (DDD-Aligned)
@SpringBootTest
@IntegrationTest  
class UserManagementServiceIT extends BoundedContextTestBase {
    @Autowired UserManagementService userManagementService;
    // Bounded context focused tests
    
    @Override
    protected void cleanupBoundedContextData() {
        // Context-specific cleanup
    }
}
```

### Pattern 2: Controller Test Migration
```java
// Before (Legacy)
@AutoConfigureMockMvc
@IntegrationTest
class UserResourceIT {
    // Generic REST tests
}

// After (DDD-Aligned)
@AutoConfigureMockMvc
@IntegrationTest
class UserManagementControllerIT extends BoundedContextTestBase {
    // Bounded context presentation tests
}
```

### Pattern 3: Domain Test Enhancement
```java
// Before (Legacy)
class UserTest {
    // Basic entity tests
}

// After (DDD-Enhanced)
class UserTest {
    // Domain behavior tests
    // Business rule validation
    // Domain event testing
}
```

## Next Steps

1. **Prioritize High-Impact Tests**: Start with most frequently used tests
2. **Batch Migration**: Group related tests for efficient migration
3. **Validate Incrementally**: Test each migration before proceeding
4. **Update CI/CD**: Adjust build scripts for new test structure
5. **Team Training**: Ensure team understands new patterns

This migration will result in a more maintainable, domain-focused test suite that aligns with your DDD architecture and supports confident refactoring and feature development.