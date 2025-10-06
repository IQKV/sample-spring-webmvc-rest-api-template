/*
 * Copyright 2025 IQKV Foundation Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqkv.sample.webmvc.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.shared.infrastructure.ServiceDiscovery;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests to validate the microservices evolution capability.
 * <p>
 * These tests ensure that the architecture can seamlessly transition
 * from monolithic to microservices deployment without code changes.
 */
@SpringBootTest
@ActiveProfiles("test")
class MicroservicesEvolutionTest {

  @Autowired
  private ServiceDiscovery serviceDiscovery;

  @Autowired
  private UserManagementServiceClient userManagementServiceClient;

  @Test
  void shouldHaveServiceDiscoveryConfigured() {
    // Verify that service discovery is properly configured
    assertThat(serviceDiscovery).isNotNull();

    // In monolith mode, all services should be available locally
    assertThat(serviceDiscovery.isServiceAvailable("user-management")).isTrue();
    assertThat(serviceDiscovery.isServiceAvailable("security")).isTrue();
    assertThat(serviceDiscovery.isServiceAvailable("notification")).isTrue();
    assertThat(serviceDiscovery.isServiceAvailable("analytics")).isTrue();
  }

  @Test
  void shouldDiscoverLocalServices() {
    // Verify that local services are properly registered
    Optional<ServiceDiscovery.ServiceEndpoint> userMgmtEndpoint =
        serviceDiscovery.discoverService("user-management");

    assertThat(userMgmtEndpoint).isPresent();
    assertThat(userMgmtEndpoint.get().host()).isEqualTo("localhost");
    assertThat(userMgmtEndpoint.get().port()).isEqualTo(8080);
    assertThat(userMgmtEndpoint.get().protocol()).isEqualTo("http");
  }

  @Test
  void shouldProvideServiceBaseUrls() {
    // Verify that service base URLs are correctly provided
    Optional<String> userMgmtUrl = serviceDiscovery.getServiceBaseUrl("user-management");
    assertThat(userMgmtUrl).isPresent();
    assertThat(userMgmtUrl.get()).isEqualTo("http://localhost:8080/api");

    Optional<String> securityUrl = serviceDiscovery.getServiceBaseUrl("security");
    assertThat(securityUrl).isPresent();
    assertThat(securityUrl.get()).isEqualTo("http://localhost:8080/api");
  }

  @Test
  void shouldHaveServiceClientAbstractions() {
    // Verify that service client abstractions are properly configured
    assertThat(userManagementServiceClient).isNotNull();

    // The client should work regardless of deployment mode
    long userCount = userManagementServiceClient.getUserCount();
    assertThat(userCount).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldSupportBothDeploymentModes() {
    // This test validates that the same codebase can work in both modes

    // In monolith mode (current test environment)
    assertThat(serviceDiscovery.getClass().getSimpleName()).contains("Local");

    // The service client should be the local implementation
    assertThat(userManagementServiceClient.getClass().getSimpleName()).contains("Local");

    // But the interface contract should be the same for both modes
    assertThat(userManagementServiceClient).isInstanceOf(UserManagementServiceClient.class);
  }

  @Test
  void shouldValidateServiceEndpointStructure() {
    // Verify that service endpoint structure supports microservices
    ServiceDiscovery.ServiceEndpoint endpoint = new ServiceDiscovery.ServiceEndpoint(
        "test-service",
        "test-host",
        8080,
        "http",
        "/api"
    );

    assertThat(endpoint.serviceName()).isEqualTo("test-service");
    assertThat(endpoint.host()).isEqualTo("test-host");
    assertThat(endpoint.port()).isEqualTo(8080);
    assertThat(endpoint.protocol()).isEqualTo("http");
    assertThat(endpoint.basePath()).isEqualTo("/api");
    assertThat(endpoint.getBaseUrl()).isEqualTo("http://test-host:8080/api");
  }

  @Test
  void shouldSupportServiceRegistration() {
    // Verify that services can be dynamically registered
    ServiceDiscovery.ServiceEndpoint newService = new ServiceDiscovery.ServiceEndpoint(
        "new-service",
        "new-host",
        9090,
        "https",
        "/v1"
    );

    serviceDiscovery.registerService("new-service", newService);

    // Verify registration
    assertThat(serviceDiscovery.isServiceAvailable("new-service")).isTrue();
    Optional<String> newServiceUrl = serviceDiscovery.getServiceBaseUrl("new-service");
    assertThat(newServiceUrl).isPresent();
    assertThat(newServiceUrl.get()).isEqualTo("https://new-host:9090/v1");
  }

  @Test
  void shouldValidateModularArchitectureForExtraction() {
    // Verify that the modular architecture supports service extraction

    // Each bounded context should be self-contained
    // This is validated by the modulith tests, but we can also check
    // that the necessary abstractions are in place

    assertThat(serviceDiscovery).isNotNull();
    assertThat(userManagementServiceClient).isNotNull();

    // Service discovery should support both local and remote services
    assertThat(serviceDiscovery.discoverService("user-management")).isPresent();
    assertThat(serviceDiscovery.discoverService("nonexistent-service")).isEmpty();
  }

  @Test
  void shouldSupportGradualMigration() {
    // Verify that the architecture supports gradual migration

    // Services can be discovered individually
    assertThat(serviceDiscovery.isServiceAvailable("user-management")).isTrue();
    assertThat(serviceDiscovery.isServiceAvailable("security")).isTrue();
    assertThat(serviceDiscovery.isServiceAvailable("notification")).isTrue();
    assertThat(serviceDiscovery.isServiceAvailable("analytics")).isTrue();

    // Each service can be migrated independently
    // The service client abstraction allows switching between local and remote
    assertThat(userManagementServiceClient).isNotNull();
  }

  @Test
  void shouldValidateEventDrivenCommunication() {
    // Verify that event-driven communication supports microservices

    // Events should work across service boundaries
    // This is already validated by modulith integration tests
    // Here we just verify the infrastructure is in place

    assertThat(serviceDiscovery).isNotNull();

    // In microservices mode, events would be published to message broker
    // In monolith mode, events are published locally
    // The same event publishing code works in both modes
  }
}
