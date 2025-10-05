# 🚀 Dashboard Backend

A modern Spring Boot application built with **Domain-Driven Design (DDD)** principles and **Modular Monolith** architecture, providing a REST API for building dashboard-style applications.

## 🏗️ Architecture

This application follows **Domain-Driven Design (DDD)** principles with a **Modular Monolith** architecture:

### 📦 Bounded Contexts

- **🔐 User Management**: User registration, authentication, profile management, and user lifecycle
- **📧 Notification**: Email notifications, SMS, push notifications, and cross-context communication
- **🔧 Shared Kernel**: Common domain concepts and base classes used across contexts

### 🎯 DDD Layers

```
📁 Bounded Context (e.g., usermanagement/)
├── 🏛️ domain/              # Domain Layer
│   ├── User.java           # Aggregate Root
│   ├── Authority.java      # Entity
│   ├── Email.java          # Value Object
│   ├── UserProfile.java    # Value Object
│   ├── UserDomainService.java      # Domain Service
│   ├── UserDomainRepository.java   # Repository Interface
│   └── event/              # Domain Events
├── 🎮 application/         # Application Layer
│   ├── UserManagementService.java  # Application Service
│   ├── UserEventHandler.java       # Event Handler
│   └── dto/                # Data Transfer Objects
├── 🔌 infrastructure/      # Infrastructure Layer
│   ├── UserRepository.java         # JPA Repository
│   ├── UserRepositoryAdapter.java  # Repository Adapter
│   └── UserManagementConfiguration.java
└── 🌐 presentation/        # Presentation Layer
    ├── UserManagementController.java
    └── PublicUserController.java
```

## 🧩 Features

### Core Features
- **🔐 Authentication & Security**: Spring Security with JWT support
- **👥 User Management**: Registration, activation, profile management, password reset
- **📧 Notification System**: Event-driven notifications across bounded contexts
- **🗄️ Data Persistence**: JPA/Hibernate with PostgreSQL
- **🔄 Database Migrations**: Liquibase for schema versioning
- **📊 Observability**: Health checks, metrics, and monitoring

### DDD Features
- **🎯 Rich Domain Models**: Business logic encapsulated in domain entities
- **📡 Domain Events**: Loose coupling between bounded contexts
- **🛡️ Domain Services**: Complex business rules and validations
- **💎 Value Objects**: Type-safe, immutable data structures
- **🔄 Event-Driven Architecture**: Asynchronous cross-context communication

## 🛠️ Tech Stack

**Core**: Java 25, Maven, Spring Boot 3.x, Spring MVC, Spring Security  
**Persistence**: JPA/Hibernate, Liquibase, PostgreSQL  
**Architecture**: DDD, Modular Monolith, Event-Driven Architecture  
**Observability**: Micrometer (Prometheus), Spring Boot Actuator  
**Testing**: JUnit 5, Testcontainers, ArchUnit, MockMvc  
**Tools**: MapStruct, Docker Compose

## ✅ Prerequisites

- Java 25 (JDK)
- Maven 3.9+ (or use the included `mvnw`/`mvnw.cmd` wrapper)
- Docker (optional, for local PostgreSQL/observability stack)

## ⚙️ Configuration

Application properties live under `src/main/resources` and are organized by profile:

- `application.yml` — common defaults
- `application-dev.yml` — local development
- `application-prod.yml` — production
- `application-tls.yml` — optional TLS setup

Common environment variables (override as needed):

- `SPRING_PROFILES_ACTIVE` — profile to run (e.g. `dev`, `prod`)
- `SPRING_DATASOURCE_URL` — JDBC URL (e.g. `jdbc:postgresql://localhost:5432/app`)
- `SPRING_DATASOURCE_USERNAME` — DB username
- `SPRING_DATASOURCE_PASSWORD` — DB password

Liquibase runs automatically on startup to apply pending migrations.

## 💻 Database setup

You can use any PostgreSQL instance, or start one locally:

1. Use an existing PostgreSQL and configure the connection via env vars or profile `application-*.yml`.

2. Start local services with Docker Compose (PostgreSQL, Prometheus, Grafana when defined):

```shell script
docker compose -f compose.yaml up -d
```

## 🚀 Run locally

Run with the Maven wrapper (preferred):

```shell script
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or on Windows PowerShell:

```powershell
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts with main class `com.iqkv.sample.webmvc.dashboard.DashboardApplication`.

## 📦 Build and run JAR

```shell script
./mvnw -DskipTests package
java -jar target/app.jar --spring.profiles.active=prod
```

The final artifact name is configured as `app` (see `pom.xml` `<finalName>`).

## 🧪 Testing Strategy

This project implements a comprehensive testing strategy aligned with DDD principles:

### 🏗️ Architecture Tests
```shell script
# Validate DDD architecture compliance
./mvnw test -Dtest="TechnicalStructureTest,DomainDrivenDesignTest"
```

- **DDD Compliance**: Validates onion architecture, bounded context isolation
- **Layer Dependencies**: Ensures proper dependency direction
- **Security Rules**: Validates authorization patterns
- **Naming Conventions**: Enforces consistent naming across codebase

### 🎯 Domain Layer Tests
```shell script
# Run domain logic tests
./mvnw test -Dtest="**/domain/**Test"
```

- **Aggregate Tests**: Business logic, state transitions, invariants
- **Value Object Tests**: Immutability, validation, equality
- **Domain Service Tests**: Complex business rules and validations
- **Event Tests**: Domain event creation and handling

### 🎮 Application Layer Tests
```shell script
# Run application service tests
./mvnw test -Dtest="**/application/**Test"
```

- **Service Tests**: Orchestration logic with mocked dependencies
- **Integration Tests**: Real database, full Spring context
- **Event Handler Tests**: Cross-context communication validation

### 🌐 Presentation Layer Tests
```shell script
# Run web layer tests
./mvnw test -Dtest="**/presentation/**Test"
```

- **Controller Tests**: REST API endpoints with MockMvc
- **Security Tests**: Authorization and authentication
- **Validation Tests**: Input validation and error handling

### 📊 Test Execution
```shell script
# Run all tests
./mvnw verify

# Run only unit tests
./mvnw test -Dtest="*Test"

# Run only integration tests
./mvnw test -Dtest="*IT"

# Generate coverage report
./mvnw jacoco:report
```

**Note**: Some tests use Testcontainers; Docker must be available for integration tests to run. JaCoCo coverage rules are enforced by the build.

## 📈 Observability

- Actuator endpoints (profile-dependent). Common ones:
  - `/actuator/health`
  - `/actuator/info`
  - `/actuator/metrics`
  - `/actuator/prometheus` (when Prometheus registry is enabled)
- Optional local stack via Docker Compose includes Prometheus and Grafana with example dashboards under `src/main/docker/grafana`.

## 🔐 Security

Security is implemented following DDD principles within the User Management bounded context:

### 🏛️ Domain-Driven Security
- **User Aggregate**: Encapsulates authentication and authorization logic
- **Authority Entity**: Manages roles and permissions
- **Domain Events**: User registration, activation, password reset events
- **Domain Services**: User validation and uniqueness checks

### 🔒 Security Features
- **JWT Authentication**: Stateless authentication with Spring Security
- **Role-Based Authorization**: Admin and user roles with method-level security
- **Password Management**: Secure password reset workflow with domain events
- **Account Lifecycle**: Registration, activation, deactivation with business rules

### 🗄️ Initial Data
Initial users and authorities are defined under `src/main/resources/config/db/00000000000000-initialize-security` and applied by Liquibase for new environments.

## 🚀 Getting Started

### 📋 API Documentation

The application provides RESTful APIs organized by bounded context:

#### User Management APIs
- `POST /api/admin/users` - Create user (Admin)
- `PUT /api/admin/users` - Update user (Admin)
- `GET /api/admin/users` - List users (Admin)
- `GET /api/admin/users/{login}` - Get user (Admin)
- `DELETE /api/admin/users/{login}` - Delete user (Admin)
- `GET /api/users` - Public user list

#### Authentication APIs
- User registration, activation, and password reset (handled by User Management service)

### 🎯 Domain Events

The application uses domain events for loose coupling between bounded contexts:

- **UserRegisteredEvent**: Triggers welcome email notification
- **UserActivatedEvent**: Triggers account activation notification  
- **PasswordResetRequestedEvent**: Triggers password reset email

### 🔄 Event Flow Example

```
1. User registers → UserRegisteredEvent published
2. Notification service listens → Creates activation email notification
3. User activates account → UserActivatedEvent published
4. Notification service listens → Creates welcome email notification
```

## 📁 Project Structure

```
src/main/java/com/iqkv/sample/webmvc/dashboard/
├── 🔧 config/                    # Application configuration
├── 🔐 security/                  # Security configuration (legacy)
├── 🌐 web/                       # Web configuration (legacy)
├── 🔗 shared/                    # Shared kernel
│   └── domain/                   # Common domain concepts
├── 👥 usermanagement/            # User Management bounded context
│   ├── domain/                   # Domain layer
│   │   ├── User.java            # Aggregate root
│   │   ├── Authority.java       # Entity
│   │   ├── Email.java           # Value object
│   │   ├── UserProfile.java     # Value object
│   │   ├── UserDomainService.java       # Domain service
│   │   ├── UserDomainRepository.java    # Repository interface
│   │   └── event/               # Domain events
│   ├── application/             # Application layer
│   │   ├── UserManagementService.java   # Application service
│   │   ├── UserEventHandler.java        # Event handler
│   │   └── dto/                 # Data transfer objects
│   ├── infrastructure/          # Infrastructure layer
│   │   ├── UserRepository.java          # JPA repository
│   │   ├── UserRepositoryAdapter.java   # Repository adapter
│   │   └── UserManagementConfiguration.java
│   └── presentation/            # Presentation layer
│       ├── UserManagementController.java
│       └── PublicUserController.java
└── 📧 notification/              # Notification bounded context
    ├── domain/                   # Domain layer
    ├── application/              # Application layer
    └── infrastructure/           # Infrastructure layer

src/test/java/                    # Test structure mirrors main
├── 🏗️ TechnicalStructureTest.java      # Architecture validation
├── 🎯 DomainDrivenDesignTest.java       # DDD compliance tests
├── 👥 usermanagement/            # User Management tests
│   ├── domain/                   # Domain layer tests
│   ├── application/              # Application layer tests
│   └── presentation/             # Presentation layer tests
└── 📧 notification/              # Notification tests
```

## 📝 Development Commands

### 🏃‍♂️ Running the Application
```shell script
# Development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# With custom database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb ./mvnw spring-boot:run
```

### 🧪 Testing Commands
```shell script
# Full test suite with coverage
./mvnw verify

# Architecture compliance tests
./mvnw test -Dtest="*StructureTest,*DomainDrivenDesignTest"

# Domain layer tests only
./mvnw test -Dtest="**/domain/**Test"

# Integration tests only
./mvnw test -Dtest="*IT"

# Specific bounded context tests
./mvnw test -Dtest="**/usermanagement/**Test"
```

### 📦 Build Commands
```shell script
# Build JAR (skip tests for faster build)
./mvnw -DskipTests package

# Build with full validation
./mvnw clean verify

# Build native image (GraalVM required)
./mvnw -Pnative -DskipTests native:compile
```

### 🔍 Code Quality
```shell script
# Generate test coverage report
./mvnw jacoco:report

# Check architecture rules
./mvnw test -Dtest="TechnicalStructureTest"

# Validate DDD patterns
./mvnw test -Dtest="DomainDrivenDesignTest"
```

## 📚 Documentation

- **[DDD Refactoring Summary](DDD_REFACTORING_SUMMARY.md)**: Detailed explanation of the DDD transformation
- **[Testing Summary](DDD_TESTING_SUMMARY.md)**: Comprehensive testing strategy and coverage
- **Architecture Tests**: Living documentation through ArchUnit tests
- **Domain Events**: See `src/main/java/**/domain/event/` packages

## 🤝 Contributing

When contributing to this project, please follow the DDD principles:

1. **Domain First**: Start with domain modeling before technical implementation
2. **Bounded Context Isolation**: Keep contexts independent and communicate via events
3. **Rich Domain Models**: Encapsulate business logic in domain entities
4. **Test Coverage**: Maintain comprehensive test coverage across all layers
5. **Architecture Compliance**: Ensure all changes pass architecture tests

### 🏗️ Adding New Features

1. **Identify Bounded Context**: Determine which context owns the feature
2. **Model Domain**: Create aggregates, entities, value objects, and domain services
3. **Define Events**: Model domain events for cross-context communication
4. **Implement Layers**: Build application, infrastructure, and presentation layers
5. **Write Tests**: Create comprehensive tests for all layers
6. **Validate Architecture**: Ensure architecture tests pass

## 📜 License

Distributed under the terms of the `LICENSE` file in this repository.
