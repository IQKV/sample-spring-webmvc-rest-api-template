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

package com.iqkv.sample.webmvc.dashboard.shared.infrastructure;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local service discovery implementation for monolithic deployment.
 * <p>
 * This implementation treats all services as local (same JVM) and provides
 * local endpoints. Used when running as a monolith.
 */
@Component
@ConditionalOnProperty(name = "app.deployment.mode", havingValue = "monolith", matchIfMissing = true)
public class LocalServiceDiscovery implements ServiceDiscovery {

  private final Map<String, ServiceEndpoint> services = new ConcurrentHashMap<>();

  public LocalServiceDiscovery() {
    // Register local services for monolithic deployment
    registerLocalServices();
  }

  @Override
  public Optional<ServiceEndpoint> discoverService(String serviceName) {
    return Optional.ofNullable(services.get(serviceName));
  }

  @Override
  public void registerService(String serviceName, ServiceEndpoint endpoint) {
    services.put(serviceName, endpoint);
  }

  @Override
  public boolean isServiceAvailable(String serviceName) {
    return services.containsKey(serviceName);
  }

  @Override
  public Optional<String> getServiceBaseUrl(String serviceName) {
    return discoverService(serviceName)
        .map(ServiceEndpoint::getBaseUrl);
  }

  private void registerLocalServices() {
    // User Management Service (local)
    registerService("user-management", new ServiceEndpoint(
        "user-management",
        "localhost",
        8080,
        "http",
        "/api"
    ));

    // Security Service (local)
    registerService("security", new ServiceEndpoint(
        "security",
        "localhost",
        8080,
        "http",
        "/api"
    ));

    // Notification Service (local)
    registerService("notification", new ServiceEndpoint(
        "notification",
        "localhost",
        8080,
        "http",
        "/api"
    ));

    // Analytics Service (local)
    registerService("analytics", new ServiceEndpoint(
        "analytics",
        "localhost",
        8080,
        "http",
        "/api"
    ));
  }
}
