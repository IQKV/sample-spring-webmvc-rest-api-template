#!/bin/bash

# Build script for microservices deployment
# This script extracts bounded contexts into separate microservices

set -e

echo "🚀 Building Dashboard Microservices..."

# Configuration
BASE_DIR=$(pwd)
SERVICES_DIR="$BASE_DIR/microservices"
DOCKER_REGISTRY="dashboard"
VERSION="latest"

# Create services directory
mkdir -p "$SERVICES_DIR"

# Function to create microservice from bounded context
create_microservice() {
    local service_name=$1
    local context_path=$2
    local port=$3
    
    echo "📦 Creating $service_name microservice..."
    
    # Create service directory
    local service_dir="$SERVICES_DIR/$service_name"
    mkdir -p "$service_dir"
    
    # Copy Spring Boot structure
    mkdir -p "$service_dir/src/main/java/com/iqkv/$service_name"
    mkdir -p "$service_dir/src/main/resources"
    mkdir -p "$service_dir/src/test/java/com/iqkv/$service_name"
    
    # Copy bounded context code
    if [ -d "src/main/java/com/iqkv/sample/webmvc/dashboard/$context_path" ]; then
        cp -r "src/main/java/com/iqkv/sample/webmvc/dashboard/$context_path"/* "$service_dir/src/main/java/com/iqkv/$service_name/"
    fi
    
    # Copy shared kernel
    if [ -d "src/main/java/com/iqkv/sample/webmvc/dashboard/shared" ]; then
        mkdir -p "$service_dir/src/main/java/com/iqkv/shared"
        cp -r "src/main/java/com/iqkv/sample/webmvc/dashboard/shared"/* "$service_dir/src/main/java/com/iqkv/shared/"
    fi
    
    # Copy test code
    if [ -d "src/test/java/com/iqkv/sample/webmvc/dashboard/$context_path" ]; then
        cp -r "src/test/java/com/iqkv/sample/webmvc/dashboard/$context_path"/* "$service_dir/src/test/java/com/iqkv/$service_name/"
    fi
    
    # Create application class
    cat > "$service_dir/src/main/java/com/iqkv/$service_name/${service_name^}Application.java" << EOF
package com.iqkv.$service_name;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@SpringBootApplication
@Modulith
public class ${service_name^}Application {
    public static void main(String[] args) {
        SpringApplication.run(${service_name^}Application.class, args);
    }
}
EOF
    
    # Create pom.xml
    cat > "$service_dir/pom.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.iqkv</groupId>
        <artifactId>boot-parent-pom</artifactId>
        <version>0.25.0-SNAPSHOT</version>
    </parent>
    
    <groupId>com.iqkv</groupId>
    <artifactId>$service_name-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>$service_name Service</name>
    <description>$service_name microservice extracted from dashboard monolith</description>
    
    <properties>
        <java.version>25</java.version>
        <start-class>com.iqkv.$service_name.${service_name^}Application</start-class>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        
        <!-- Spring Modulith -->
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-starter-core</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF
    
    # Create application.yml
    cat > "$service_dir/src/main/resources/application.yml" << EOF
server:
  port: $port

spring:
  application:
    name: $service_name-service
  
  profiles:
    active: microservices
  
  datasource:
    url: jdbc:postgresql://localhost:5432/${service_name}_db
    username: \${DB_USERNAME:dashboard_user}
    password: \${DB_PASSWORD:dashboard_pass}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  rabbitmq:
    host: \${RABBITMQ_HOST:localhost}
    port: \${RABBITMQ_PORT:5672}
    username: \${RABBITMQ_USERNAME:guest}
    password: \${RABBITMQ_PASSWORD:guest}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,modulith
  endpoint:
    health:
      show-details: always

app:
  deployment:
    mode: microservices

logging:
  level:
    com.iqkv.$service_name: INFO
    org.springframework.modulith: DEBUG
EOF
    
    # Create Dockerfile
    cat > "$service_dir/Dockerfile" << EOF
FROM openjdk:25-jdk-slim

WORKDIR /app

COPY target/$service_name-service-*.jar app.jar

EXPOSE $port

ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
    
    # Build the service
    echo "🔨 Building $service_name service..."
    cd "$service_dir"
    
    # Fix package declarations in copied files
    find src -name "*.java" -type f -exec sed -i "s/package com\.iqkv\.sample\.webmvc\.dashboard\.$context_path/package com.iqkv.$service_name/g" {} \;
    find src -name "*.java" -type f -exec sed -i "s/import com\.iqkv\.sample\.webmvc\.dashboard\.$context_path/import com.iqkv.$service_name/g" {} \;
    find src -name "*.java" -type f -exec sed -i "s/import com\.iqkv\.sample\.webmvc\.dashboard\.shared/import com.iqkv.shared/g" {} \;
    
    # Build with Maven
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
        
        # Build Docker image
        if [ -f "target/$service_name-service-1.0.0-SNAPSHOT.jar" ]; then
            docker build -t "$DOCKER_REGISTRY/$service_name-service:$VERSION" .
            echo "✅ $service_name service built successfully"
        else
            echo "❌ Failed to build $service_name service"
        fi
    else
        echo "⚠️  Maven not found, skipping build for $service_name"
    fi
    
    cd "$BASE_DIR"
}

# Create API Gateway
create_api_gateway() {
    echo "🌐 Creating API Gateway..."
    
    local gateway_dir="$SERVICES_DIR/api-gateway"
    mkdir -p "$gateway_dir"
    
    # Create basic Spring Cloud Gateway structure
    mkdir -p "$gateway_dir/src/main/java/com/iqkv/gateway"
    mkdir -p "$gateway_dir/src/main/resources"
    
    # Create gateway application
    cat > "$gateway_dir/src/main/java/com/iqkv/gateway/ApiGatewayApplication.java" << 'EOF'
package com.iqkv.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-management", r -> r.path("/api/users/**", "/api/account/**", "/api/authorities/**")
                .uri("http://user-management-service:8081"))
            .route("security", r -> r.path("/api/authenticate/**", "/api/security/**")
                .uri("http://security-service:8082"))
            .route("notification", r -> r.path("/api/notifications/**")
                .uri("http://notification-service:8083"))
            .route("analytics", r -> r.path("/api/analytics/**", "/api/activities/**")
                .uri("http://analytics-service:8084"))
            .build();
    }
}
EOF
    
    echo "✅ API Gateway created"
}

# Main execution
echo "🏗️  Starting microservices build process..."

# Create microservices
create_microservice "usermanagement" "usermanagement" "8081"
create_microservice "security" "security" "8082"
create_microservice "notification" "notification" "8083"
create_microservice "analytics" "analytics" "8084"

# Create API Gateway
create_api_gateway

# Create docker-compose for local development
echo "🐳 Creating Docker Compose configuration..."
cp docker/microservices/docker-compose.yml "$SERVICES_DIR/"

echo ""
echo "🎉 Microservices build complete!"
echo ""
echo "📁 Services created in: $SERVICES_DIR"
echo "🐳 Docker images built with tag: $VERSION"
echo ""
echo "🚀 To start the microservices:"
echo "   cd $SERVICES_DIR"
echo "   docker-compose up -d"
echo ""
echo "🔍 To verify services:"
echo "   curl http://localhost:8081/actuator/health  # User Management"
echo "   curl http://localhost:8082/actuator/health  # Security"
echo "   curl http://localhost:8083/actuator/health  # Notification"
echo "   curl http://localhost:8084/actuator/health  # Analytics"
echo "   curl http://localhost:8080/actuator/health  # API Gateway"