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

package com.iqkv.sample.webmvc.dashboard.usermanagement.application;

import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;

/**
 * Service client interface for User Management operations.
 * <p>
 * This interface provides a clean abstraction for accessing user management
 * functionality that can be implemented for both monolithic (local) and
 * microservices (remote) deployment scenarios.
 */
public interface UserManagementServiceClient {

  /**
   * Gets the current authenticated user.
   *
   * @return the current user if authenticated
   */
  Optional<UserDTO> getCurrentUser();

  /**
   * Gets a user by login.
   *
   * @param login the user login
   * @return the user if found
   */
  Optional<UserDTO> getUserByLogin(String login);

  /**
   * Gets a user by email.
   *
   * @param email the user email
   * @return the user if found
   */
  Optional<UserDTO> getUserByEmail(String email);

  /**
   * Checks if a user exists by login.
   *
   * @param login the user login
   * @return true if user exists
   */
  boolean userExistsByLogin(String login);

  /**
   * Checks if a user exists by email.
   *
   * @param email the user email
   * @return true if user exists
   */
  boolean userExistsByEmail(String email);

  /**
   * Gets the total number of users.
   *
   * @return the total user count
   */
  long getUserCount();

  /**
   * Checks if a user has a specific authority.
   *
   * @param login     the user login
   * @param authority the authority to check
   * @return true if user has the authority
   */
  boolean userHasAuthority(String login, String authority);
}
