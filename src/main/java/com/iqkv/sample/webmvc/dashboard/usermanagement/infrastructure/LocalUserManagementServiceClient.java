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

import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementFacade;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementServiceClient;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local implementation of UserManagementServiceClient for monolithic deployment.
 * <p>
 * This implementation directly calls local services within the same JVM.
 */
@Component
@ConditionalOnProperty(name = "app.deployment.mode", havingValue = "monolith", matchIfMissing = true)
public class LocalUserManagementServiceClient implements UserManagementServiceClient {

  private final UserManagementFacade userManagementFacade;
  private final UserManagementService userManagementService;

  public LocalUserManagementServiceClient(
      UserManagementFacade userManagementFacade,
      UserManagementService userManagementService) {
    this.userManagementFacade = userManagementFacade;
    this.userManagementService = userManagementService;
  }

  @Override
  public Optional<UserDTO> getCurrentUser() {
    return userManagementFacade.getCurrentUser();
  }

  @Override
  public Optional<UserDTO> getUserByLogin(String login) {
    return userManagementService.getUserWithAuthoritiesByLogin(login)
        .map(this::convertToDTO);
  }

  @Override
  public Optional<UserDTO> getUserByEmail(String email) {
    return userManagementService.getUserWithAuthoritiesByEmail(email)
        .map(this::convertToDTO);
  }

  @Override
  public boolean userExistsByLogin(String login) {
    return userManagementService.getUserWithAuthoritiesByLogin(login).isPresent();
  }

  @Override
  public boolean userExistsByEmail(String email) {
    return userManagementService.getUserWithAuthoritiesByEmail(email).isPresent();
  }

  @Override
  public long getUserCount() {
    return userManagementService.countUsers();
  }

  @Override
  public boolean userHasAuthority(String login, String authority) {
    return userManagementService.getUserWithAuthoritiesByLogin(login)
        .map(user -> user.hasAuthority(authority))
        .orElse(false);
  }

  private UserDTO convertToDTO(User user) {
    return new UserDTO(user);
  }
}
