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

import java.util.Optional;

/**
 * Service discovery abstraction for microservices evolution.
 * <p>
 * This interface provides a clean abstraction for service location
 * that can be implemented for monolithic (local) or microservices (remote) deployment.
 */
public interface ServiceDiscovery {

  /**
   * Discovers a service by name.
   *
   * @param serviceName the name of the service to discover
   * @return service endpoint information if found
   */
  Optional<ServiceEndpoint> discoverService(String serviceName);

  /**
   * Registers a service endpoint.
   *
   * @param serviceName the name of the service
   * @param endpoint    the service endpoint
   */
  void registerService(String serviceName, ServiceEndpoint endpoint);

  /**
   * Checks if a service is available.
   *
   * @param serviceName the name of the service
   * @return true if service is available
   */
  boolean isServiceAvailable(String serviceName);

  /**
   * Gets the base URL for a service.
   *
   * @param serviceName the name of the service
   * @return the base URL for the service
   */
  Optional<String> getServiceBaseUrl(String serviceName);

  /**
   * Service endpoint information.
   */
  record ServiceEndpoint(
      String serviceName,
      String host,
      int port,
      String protocol,
      String basePath
  ) {
    public String getBaseUrl() {
      return protocol + "://" + host + ":" + port + basePath;
    }
  }
}
