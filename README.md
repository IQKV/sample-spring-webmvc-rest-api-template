# 🚀 Dashboard Backend

Backend service providing a REST API for building dashboard-style applications.

## 🧩 Features

- Authentication and security via Spring Security
- Relational persistence with JPA/Hibernate
- Database migrations with Liquibase
- Ready-to-use dev/prod Spring profiles
- Health, metrics, and info endpoints (Spring Boot Actuator)
- Prometheus metrics and optional Grafana dashboards via Docker Compose

## 🛠️ Tech stack

Java 21, Maven, Spring Boot, Spring MVC, Spring Security, JPA/Hibernate, Liquibase, PostgreSQL, Micrometer (Prometheus), MapStruct, Testcontainers (tests).

## ✅ Prerequisites

- Java 21 (JDK)
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

The app starts with main class `com.github.dimdnk.sample.webmvc.dashboard.DashboardApplication`.

## 📦 Build and run JAR

```shell script
./mvnw -DskipTests package
java -jar target/app.jar --spring.profiles.active=prod
```

The final artifact name is configured as `app` (see `pom.xml` `<finalName>`).

## 🧪 Tests and coverage

- Unit and integration tests: `./mvnw verify`
- Some tests use Testcontainers; Docker must be available for those to run
- JaCoCo coverage rules are enforced by the build (see `pom.xml`)

## 📈 Observability

- Actuator endpoints (profile-dependent). Common ones:
  - `/actuator/health`
  - `/actuator/info`
  - `/actuator/metrics`
  - `/actuator/prometheus` (when Prometheus registry is enabled)
- Optional local stack via Docker Compose includes Prometheus and Grafana with example dashboards under `src/main/docker/grafana`.

## 🔐 Security

Security is provided by Spring Security and project starters. Configure users/authorities and authentication according to your environment. Initial data for users and authorities is defined under `src/main/resources/config/db/00000000000000-initialize-security` and applied by Liquibase for new environments.

## 📝 Useful Maven commands

- Format/compile and run tests: `./mvnw verify`
- Run with a specific profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- Build native image (if GraalVM is available/configured): `./mvnw -Pnative -DskipTests native:compile`

## 📜 License

Distributed under the terms of the `LICENSE` file in this repository.
