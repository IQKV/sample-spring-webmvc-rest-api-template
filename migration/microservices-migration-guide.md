# 🚀 Microservices Migration Guide

## Overview

This guide provides step-by-step instructions for migrating from the current modular monolith to a microservices architecture. The migration is designed to be gradual and low-risk.

## Migration Strategy

### Phase 1: Preparation (Weeks 1-2)
- ✅ **Already Complete**: Modular monolith with proper bounded contexts
- ✅ **Already Complete**: Event-driven communication between modules
- ✅ **Already Complete**: Service client abstractions
- ✅ **Already Complete**: Configuration profiles for different deployment modes

### Phase 2: Infrastructure Setup (Weeks 3-4)
1. **Set up infrastructure components**:
   - Service discovery (Eureka/Consul)
   - Message broker (RabbitMQ/Apache Kafka)
   - API Gateway (Spring Cloud Gateway)
   - Monitoring stack (Prometheus/Grafana)
   - Distributed tracing (Zipkin/Jaeger)

2. **Database separation**:
   - Create separate databases for each bounded context
   - Implement data migration scripts
   - Set up database per service pattern

### Phase 3: Service Extraction (Weeks 5-8)
Extract services in the following order (least dependent first):

#### 3.1 Analytics Service (Week 5)
```bash
# 1. Create new Spring Boot application
spring init --dependencies=web,jpa,actuator analytics-service

# 2. Copy analytics bounded context code
cp -r src/main/java/com/iqkv/sample/webmvc/dashboard/analytics/* analytics-service/src/main/java/com/iqkv/analytics/

# 3. Update configuration
cp src/main/resources/application-microservices.yml analytics-service/src/main/resources/application.yml

# 4. Build and deploy
cd analytics-service
./mvnw clean package
docker build -t dashboard/analytics-service:latest .
```

#### 3.2 Notification Service (Week 6)
```bash
# Similar process for notification service
spring init --dependencies=web,jpa,actuator,amqp notification-service
# Copy notification bounded context
# Configure message broker integration
# Deploy as microservice
```

#### 3.3 Security Service (Week 7)
```bash
# Extract security service
spring init --dependencies=web,jpa,actuator,security security-service
# Copy security bounded context
# Configure authentication/authorization
# Deploy as microservice
```

#### 3.4 User Management Service (Week 8)
```bash
# Extract user management service (most complex)
spring init --dependencies=web,jpa,actuator,security user-management-service
# Copy user management bounded context
# Configure as core identity service
# Deploy as microservice
```

### Phase 4: API Gateway Setup (Week 9)
1. **Deploy API Gateway**:
   - Route requests to appropriate services
   - Implement authentication/authorization
   - Add rate limiting and circuit breakers
   - Configure load balancing

2. **Update client applications**:
   - Point to API Gateway instead of monolith
   - Update service discovery configuration

### Phase 5: Monitoring and Observability (Week 10)
1. **Implement distributed tracing**
2. **Set up centralized logging**
3. **Configure service health checks**
4. **Implement alerting and monitoring dashboards**

## Migration Commands

### 1. Switch to Microservices Mode
```bash
# Update application properties
echo "app.deployment.mode=microservices" >> src/main/resources/application.yml

# Start with microservices profile
java -jar dashboard-app.jar --spring.profiles.active=microservices
```

### 2. Database Migration
```sql
-- Create separate databases
CREATE DATABASE user_management_db;
CREATE DATABASE security_db;
CREATE DATABASE notification_db;
CREATE DATABASE analytics_db;

-- Migrate data (example for user management)
INSERT INTO user_management_db.knowhow_user 
SELECT * FROM dashboard_db.knowhow_user;

INSERT INTO user_management_db.knowhow_authority 
SELECT * FROM dashboard_db.knowhow_authority;
```

### 3. Service Deployment
```bash
# Build all services
./build-microservices.sh

# Deploy infrastructure
docker-compose -f docker/microservices/docker-compose.yml up -d

# Deploy services
kubectl apply -f k8s/microservices/
```

## Configuration Changes

### Monolith Configuration
```yaml
# application-monolith.yml
app:
  deployment:
    mode: monolith
services:
  user-management:
    url: http://localhost:8080
    local: true
```

### Microservices Configuration
```yaml
# application-microservices.yml
app:
  deployment:
    mode: microservices
services:
  user-management:
    url: http://user-management-service:8081
    timeout: 30s
    retry-attempts: 3
```

## Testing Strategy

### 1. Module Testing (Already Complete)
- ✅ Individual module tests with `@ApplicationModuleTest`
- ✅ Event-driven communication tests
- ✅ Bounded context isolation validation

### 2. Integration Testing
```java
// Test service client abstractions
@Test
void shouldWorkWithBothLocalAndRemoteClients() {
    // Test with local client (monolith mode)
    UserManagementServiceClient localClient = new LocalUserManagementServiceClient(...);
    
    // Test with remote client (microservices mode)
    UserManagementServiceClient remoteClient = new RemoteUserManagementServiceClient(...);
    
    // Both should provide same functionality
    assertThat(localClient.getCurrentUser()).isEqualTo(remoteClient.getCurrentUser());
}
```

### 3. End-to-End Testing
```bash
# Test complete user flow across services
curl -X POST http://api-gateway:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"login":"testuser","email":"test@example.com"}'

# Verify event propagation
curl http://notification-service:8083/api/notifications?recipient=test@example.com
curl http://analytics-service:8084/api/activities?userId=1
```

## Rollback Strategy

### 1. Immediate Rollback
```bash
# Switch back to monolith mode
export SPRING_PROFILES_ACTIVE=monolith
java -jar dashboard-app.jar

# Or update configuration
kubectl patch configmap app-config -p '{"data":{"app.deployment.mode":"monolith"}}'
```

### 2. Data Rollback
```sql
-- Merge data back to monolith database if needed
INSERT INTO dashboard_db.knowhow_user 
SELECT * FROM user_management_db.knowhow_user 
WHERE created_date > '2025-01-01';
```

## Monitoring and Validation

### 1. Service Health
```bash
# Check service health
curl http://user-management-service:8081/actuator/health
curl http://security-service:8082/actuator/health
curl http://notification-service:8083/actuator/health
curl http://analytics-service:8084/actuator/health
```

### 2. Event Flow Validation
```bash
# Check event publication and consumption
curl http://user-management-service:8081/actuator/modulith/events
curl http://notification-service:8083/actuator/modulith/events
```

### 3. Performance Monitoring
- Monitor response times across services
- Track event processing latency
- Validate database performance per service
- Monitor resource usage per service

## Success Criteria

### Technical Criteria
- [ ] All services deploy independently
- [ ] Event-driven communication works across services
- [ ] API Gateway routes requests correctly
- [ ] Database per service pattern implemented
- [ ] Monitoring and observability in place

### Business Criteria
- [ ] No downtime during migration
- [ ] All existing functionality preserved
- [ ] Performance meets or exceeds monolith
- [ ] Team can develop services independently
- [ ] Rollback capability maintained

## Risk Mitigation

### 1. Gradual Migration
- Extract one service at a time
- Maintain monolith as fallback
- Use feature flags for service routing

### 2. Data Consistency
- Implement saga pattern for distributed transactions
- Use event sourcing for audit trails
- Maintain data synchronization during transition

### 3. Service Communication
- Implement circuit breakers
- Add retry mechanisms with exponential backoff
- Use bulkhead pattern for isolation

## Post-Migration Tasks

### 1. Optimization
- Fine-tune service configurations
- Optimize database queries per service
- Implement caching strategies
- Configure auto-scaling

### 2. Team Organization
- Assign service ownership to teams
- Establish service development guidelines
- Implement service governance policies
- Set up continuous deployment pipelines

### 3. Documentation
- Update architecture documentation
- Create service API documentation
- Document deployment procedures
- Maintain troubleshooting guides

## Conclusion

This migration strategy leverages the existing modular monolith architecture to enable a smooth transition to microservices. The key advantages:

1. **Low Risk**: Gradual migration with rollback capability
2. **Proven Architecture**: Built on validated DDD bounded contexts
3. **Event-Driven**: Already using proper inter-service communication
4. **Service Abstractions**: Client interfaces ready for both modes
5. **Comprehensive Testing**: Modulith tests validate service boundaries

The migration can be executed with minimal business disruption while providing the benefits of microservices architecture when needed.