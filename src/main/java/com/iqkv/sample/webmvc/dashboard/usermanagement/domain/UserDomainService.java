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

package com.iqkv.sample.webmvc.dashboard.usermanagement.domain;

import org.springframework.stereotype.Service;

/**
 * Domain service for user-related business logic that doesn't naturally fit
 * within a single aggregate or involves multiple aggregates.
 */
@Service
public class UserDomainService {

  private final UserDomainRepository userRepository;

  public UserDomainService(UserDomainRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Validates that a login is unique.
   *
   * @param login the login to validate
   * @throws IllegalArgumentException if login already exists
   */
  public void validateUniqueLogin(String login) {
    if (userRepository.existsByLogin(login.toLowerCase())) {
      throw new IllegalArgumentException("Login already exists: " + login);
    }
  }

  /**
   * Validates that an email is unique.
   *
   * @param email the email to validate
   * @throws IllegalArgumentException if email already exists
   */
  public void validateUniqueEmail(String email) {
    if (userRepository.existsByEmail(email.toLowerCase())) {
      throw new IllegalArgumentException("Email already exists: " + email);
    }
  }

  /**
   * Validates that a login is unique for a specific user (excluding the user itself).
   *
   * @param login  the login to validate
   * @param userId the user ID to exclude from validation
   * @throws IllegalArgumentException if login already exists for another user
   */
  public void validateUniqueLoginForUser(String login, Long userId) {
    userRepository.findByLogin(login.toLowerCase())
        .filter(user -> !user.getId().equals(userId))
        .ifPresent(user -> {
          throw new IllegalArgumentException("Login already exists: " + login);
        });
  }

  /**
   * Validates that an email is unique for a specific user (excluding the user itself).
   *
   * @param email  the email to validate
   * @param userId the user ID to exclude from validation
   * @throws IllegalArgumentException if email already exists for another user
   */
  public void validateUniqueEmailForUser(String email, Long userId) {
    userRepository.findByEmail(email.toLowerCase())
        .filter(user -> !user.getId().equals(userId))
        .ifPresent(user -> {
          throw new IllegalArgumentException("Email already exists: " + email);
        });
  }
}
