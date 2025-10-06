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
 * Facade interface for User Management bounded context.
 * Provides a clean contract for other bounded contexts to interact with user management functionality.
 */
public interface UserManagementFacade {

  /**
   * Find a user by their unique identifier.
   *
   * @param id the user ID
   * @return the user DTO if found
   */
  Optional<UserDTO> findUserById(Long id);

  /**
   * Find a user by their login.
   *
   * @param login the user login
   * @return the user DTO if found
   */
  Optional<UserDTO> findUserByLogin(String login);

  /**
   * Check if a user exists by login.
   *
   * @param login the user login
   * @return true if user exists, false otherwise
   */
  boolean userExists(String login);

  /**
   * Check if a user exists by email.
   *
   * @param email the user email
   * @return true if user exists, false otherwise
   */
  boolean userExistsByEmail(String email);

  /**
   * Get the current authenticated user.
   *
   * @return the current user DTO if authenticated
   */
  Optional<UserDTO> getCurrentUser();

  /**
   * Check if a user is activated.
   *
   * @param login the user login
   * @return true if user is activated, false otherwise
   */
  boolean isUserActivated(String login);
}