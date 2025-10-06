# 🏗️ DDD Structure Refactoring Plan

## Current State Analysis

The project is in a **hybrid state** with both legacy layered architecture and new DDD structure coexisting. This creates:
- Code duplication (User, Authority entities)
- Inconsistent patterns (old vs new repositories)
- Architectural confusion (mixed concerns)
- Maintenance overhead (two patterns to maintain)

## 🎯 Refactoring Goals

1. **Complete DDD Migration**: Remove legacy layered structure
2. **Eliminate Duplication**: Consolidate domain models
3. **Strengthen Bounded Contexts**: Improve context boundaries
4. **Clean Architecture**: Proper dependency direction
5. **Consistent Patterns**: Single approach throughout

## 📦 Target Structure

### Bounded Contexts Organization
```
src/main/java/com/iqkv/sample/webmvc/dashboard/
├── 🏛️ shared/                          # Shared Kernel
│   ├── domain/                         # Common domain concepts
│   │   ├── AbstractAuditingEntity.java
│   │   ├── DomainEvent.java
│   │   └── ValueObject.java
│   ├── infrastructure/                 # Shared infrastructure
│   │   ├── EventPublisher.java
│   │   ├── MailService.java
│   │   └── CacheService.java
│   └── application/                    # Cross-cutting services
│       └── ApplicationEventPublisher.java
├── 👥 usermanagement/                  # User Management BC
│   ├── domain/                         # Rich domain models
│   │   ├── User.java                   # Aggregate Root
│   │   ├── Authority.java              # Entity
│   │   ├── Email.java                  # Value Object
│   │   ├── UserProfile.java            # Value Object
│   │   ├── UserDomainService.java      # Domain Service
│   │   ├── UserDomainRepository.java   # Repository Interface
│   │   └── event/                      # Domain Events
│   ├── application/                    # Application Services
│   │   ├── UserManagementService.java
│   │   ├── UserEventHandler.java
│   │   ├── dto/                        # DTOs
│   │   └── mapper/                     # Mappers
│   ├── infrastructure/                 # Infrastructure Layer
│   │   ├── UserRepository.java         # JPA Repository
│   │   ├── UserRepositoryAdapter.java  # Adapter
│   │   └── UserManagementConfiguration.java
│   └── presentation/                   # Presentation Layer
│       ├── UserManagementController.java
│       ├── AccountController.java
│       └── vm/                         # View Models
├── 🔐 security/                        # Security BC
│   ├── domain/                         # Security domain
│   │   ├── SecurityEvent.java
│   │   ├── AuthenticationAttempt.java
│   │   ├── SecurityPolicy.java
│   │   └── ThreatAssessment.java
│   ├── application/                    # Security services
│   │   ├── AuthenticationService.java
│   │   ├── AuthorizationService.java
│   │   └── SecurityMetricsService.java
│   ├── infrastructure/                 # Security infrastructure
│   │   ├── JwtTokenProvider.java
│   │   ├── DomainUserDetailsService.java
│   │   └── SecurityConfiguration.java
│   └── presentation/                   # Security endpoints
│       └── AuthenticationController.java
├── 📧 notification/                    # Notification BC
│   ├── domain/                         # Notification domain
│   ├── application/                    # Notification services
│   ├── infrastructure/                 # Email/SMS providers
│   └── presentation/                   # Notification APIs
├── 📊 analytics/                       # Analytics BC
│   ├── domain/                         # Analytics domain
│   ├── application/                    # Analytics services
│   ├── infrastructure/                 # Metrics storage
│   └── presentation/                   # Analytics APIs
└── 🚀 DashboardApplication.java        # Application entry point
```

## 🔄 Migration Steps

### Phase 1: Consolidate Domain Models (High Priority)

#### 1.1 Remove Legacy Domain Entities
```bash
# Files to remove (duplicates):
- src/main/java/com/iqkv/sample/webmvc/dashboard/domain/User.java
- src/main/java/com/iqkv/sample/webmvc/dashboard/domain/Authority.java
- src/main/java/com/iqkv/sample/webmvc/dashboard/domain/AbstractAuditingEntity.java
```

#### 1.2 Update Import References
- Update all imports to use `usermanagement.domain.User`
- Update all imports to use `usermanagement.domain.Authority`
- Update all imports to use `shared.domain.AbstractAuditingEntity`

#### 1.3 Consolidate Repository Pattern
```bash
# Files to remove (legacy repositories):
- src/main/java/com/iqkv/sample/webmvc/dashboard/repository/UserRepository.java
- src/main/java/com/iqkv/sample/webmvc/dashboard/repository/AuthorityRepository.java
```

### Phase 2: Complete Security Bounded Context

#### 2.1 Create Missing Security Domain Models
```java
// security/domain/AuthenticationAttempt.java
// security/domain/SecurityPolicy.java
// security/domain/ThreatAssessment.java
```

#### 2.2 Move Security Services
```bash
# Move to security bounded context:
- management/SecurityMetersService.java → security/application/SecurityMetricsService.java
- security/DomainUserDetailsService.java → security/infrastructure/DomainUserDetailsService.java
- security/SpringSecurityAuditorAware.java → security/infrastructure/SpringSecurityAuditorAware.java
```

#### 2.3 Create Security Application Services
```java
// security/application/AuthenticationService.java
// security/application/AuthorizationService.java
// security/application/SecurityEventHandler.java
```

### Phase 3: Migrate Legacy Web Layer

#### 3.1 Remove Legacy Controllers
```bash
# Files to remove (replaced by presentation layer):
- web/rest/UserResource.java → Already replaced by usermanagement/presentation/
- web/rest/AccountResource.java → Already replaced by usermanagement/presentation/
- web/rest/AuthenticateController.java → Move to security/presentation/
- web/rest/AuthorityResource.java → Move to usermanagement/presentation/
- web/rest/PublicUserResource.java → Already replaced
```

#### 3.2 Migrate Remaining Controllers
```java
// Move AuthenticateController to security/presentation/AuthenticationController.java
// Move AuthorityResource to usermanagement/presentation/AuthorityController.java
```

### Phase 4: Reorganize Configuration

#### 4.1 Move Infrastructure Configuration
```bash
# Distribute configuration by bounded context:
- config/SecurityConfiguration.java → security/infrastructure/SecurityConfiguration.java
- config/SecurityJwtConfiguration.java → security/infrastructure/JwtConfiguration.java
- config/DatabaseConfiguration.java → shared/infrastructure/DatabaseConfiguration.java
```

#### 4.2 Keep Application-Level Configuration
```bash
# Keep in config/ (application-wide concerns):
- config/ApplicationProperties.java
- config/AsyncConfiguration.java
- config/CacheConfiguration.java
- config/WebConfigurer.java
```

### Phase 5: Strengthen Bounded Context Boundaries

#### 5.1 Create Bounded Context Interfaces
```java
// Define clear contracts between contexts
public interface UserManagementFacade {
    UserDTO findUserById(Long id);
    boolean userExists(String login);
}

public interface NotificationFacade {
    void sendNotification(NotificationRequest request);
}
```

#### 5.2 Implement Anti-Corruption Layers
```java
// Prevent direct dependencies between contexts
@Component
public class UserManagementFacadeImpl implements UserManagementFacade {
    // Translate between contexts
}
```

## 🛠️ Implementation Priority

### 🔥 High Priority (Week 1)
1. **Remove duplicate domain entities** (User, Authority)
2. **Consolidate repository pattern** (remove legacy repositories)
3. **Update all import references**
4. **Complete security bounded context**

### 🔶 Medium Priority (Week 2)
1. **Migrate legacy web controllers**
2. **Reorganize configuration by context**
3. **Create bounded context facades**
4. **Implement anti-corruption layers**

### 🔵 Low Priority (Week 3)
1. **Add missing domain services**
2. **Enhance event-driven communication**
3. **Improve cross-context integration**
4. **Add architectural tests**

## 🎯 Expected Benefits

### Immediate Benefits
- **Eliminated Duplication**: Single source of truth for domain models
- **Consistent Patterns**: One approach throughout the application
- **Cleaner Architecture**: Proper dependency direction
- **Reduced Complexity**: Fewer concepts to understand

### Long-term Benefits
- **Better Maintainability**: Changes isolated to specific contexts
- **Improved Testability**: Clear boundaries for testing
- **Enhanced Scalability**: Easy to extract microservices later
- **Team Productivity**: Clear ownership and responsibilities

## 🚨 Migration Risks & Mitigation

### Risk 1: Breaking Changes
**Mitigation**: 
- Maintain backward compatibility during migration
- Use feature flags for gradual rollout
- Comprehensive testing at each step

### Risk 2: Import Reference Updates
**Mitigation**:
- Use IDE refactoring tools
- Automated search and replace
- Compile-time verification

### Risk 3: Configuration Dependencies
**Mitigation**:
- Move configuration incrementally
- Test each configuration change
- Maintain application functionality

## 📋 Success Criteria

### Technical Criteria
- [ ] Zero duplicate domain entities
- [ ] Single repository pattern throughout
- [ ] All legacy web controllers removed
- [ ] Configuration organized by context
- [ ] All tests passing

### Architectural Criteria
- [ ] Clear bounded context boundaries
- [ ] Proper dependency direction
- [ ] Event-driven communication
- [ ] Anti-corruption layers implemented
- [ ] Architectural tests enforcing constraints

### Quality Criteria
- [ ] Code coverage maintained
- [ ] Performance not degraded
- [ ] Documentation updated
- [ ] Team training completed
- [ ] Monitoring and alerting updated

## 🔄 Rollback Plan

If issues arise during migration:

1. **Immediate Rollback**: Revert specific commits
2. **Partial Rollback**: Keep completed phases, rollback problematic ones
3. **Configuration Rollback**: Restore original configuration files
4. **Database Rollback**: No schema changes, so no database rollback needed

## 📚 Next Steps

1. **Review and Approve Plan**: Team review of migration strategy
2. **Create Feature Branch**: `feature/ddd-structure-cleanup`
3. **Implement Phase 1**: Start with high-priority items
4. **Continuous Testing**: Run full test suite after each change
5. **Monitor Progress**: Track completion against success criteria

This refactoring will complete the DDD transformation and provide a clean, maintainable architecture that truly follows Domain-Driven Design principles.