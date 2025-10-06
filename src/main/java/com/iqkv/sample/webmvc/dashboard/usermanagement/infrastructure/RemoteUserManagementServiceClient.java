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

package com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure;

import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.shared.infrastructure.ServiceDiscovery;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementServiceClient;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Remote implementation of UserManagementServiceClient for microservices deployment.
 * <p>
 * This implementation makes HTTP calls to the remote User Management microservice.
 */
@Component
@ConditionalOnProperty(name = "app.deployment.mode", havingValue = "microservices")
public class RemoteUserManagementServiceClient implements UserManagementServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(RemoteUserManagementServiceClient.class);
  private static final String SERVICE_NAME = "user-management";

  private final ServiceDiscovery serviceDiscovery;
  private final RestTemplate restTemplate;

  public RemoteUserManagementServiceClient(ServiceDiscovery serviceDiscovery, RestTemplate restTemplate) {
    this.serviceDiscovery = serviceDiscovery;
    this.restTemplate = restTemplate;
  }

  @Override
  public Optional<UserDTO> getCurrentUser() {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/account";

      UserDTO user = restTemplate.getForObject(url, UserDTO.class);
      return Optional.ofNullable(user);
    } catch (Exception e) {
      LOG.error("Failed to get current user from remote service", e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserDTO> getUserByLogin(String login) {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/users/" + login;

      UserDTO user = restTemplate.getForObject(url, UserDTO.class);
      return Optional.ofNullable(user);
    } catch (Exception e) {
      LOG.error("Failed to get user by login from remote service: {}", login, e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserDTO> getUserByEmail(String email) {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/users/by-email?email=" + email;

      UserDTO user = restTemplate.getForObject(url, UserDTO.class);
      return Optional.ofNullable(user);
    } catch (Exception e) {
      LOG.error("Failed to get user by email from remote service: {}", email, e);
      return Optional.empty();
    }
  }

  @Override
  public boolean userExistsByLogin(String login) {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/users/" + login + "/exists";

      Boolean exists = restTemplate.getForObject(url, Boolean.class);
      return Boolean.TRUE.equals(exists);
    } catch (Exception e) {
      LOG.error("Failed to check user existence by login from remote service: {}", login, e);
      return false;
    }
  }

  @Override
  public boolean userExistsByEmail(String email) {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/users/by-email/" + email + "/exists";

      Boolean exists = restTemplate.getForObject(url, Boolean.class);
      return Boolean.TRUE.equals(exists);
    } catch (Exception e) {
      LOG.error("Failed to check user existence by email from remote service: {}", email, e);
      return false;
    }
  }

  @Override
  public long getUserCount() {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/users/count";

      Long count = restTemplate.getForObject(url, Long.class);
      return count != null ? count : 0L;
    } catch (Exception e) {
      LOG.error("Failed to get user count from remote service", e);
      return 0L;
    }
  }

  @Override
  public boolean userHasAuthority(String login, String authority) {
    try {
      String baseUrl = getServiceBaseUrl();
      String url = baseUrl + "/users/" + login + "/authorities/" + authority;

      Boolean hasAuthority = restTemplate.getForObject(url, Boolean.class);
      return Boolean.TRUE.equals(hasAuthority);
    } catch (Exception e) {
      LOG.error("Failed to check user authority from remote service: {} - {}", login, authority, e);
      return false;
    }
  }

  private String getServiceBaseUrl() {
    return serviceDiscovery.getServiceBaseUrl(SERVICE_NAME)
        .orElseThrow(() -> new IllegalStateException("User Management service not available"));
  }
}
