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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain repository interface for User aggregate.
 * This interface defines the contract for user persistence operations
 * without exposing infrastructure concerns.
 */
public interface UserDomainRepository {

  /**
   * Saves a user.
   *
   * @param user the user to save
   * @return the saved user
   */
  User save(User user);

  /**
   * Finds a user by ID.
   *
   * @param id the user ID
   * @return optional user
   */
  Optional<User> findById(Long id);

  /**
   * Finds a user by login.
   *
   * @param login the login
   * @return optional user
   */
  Optional<User> findByLogin(String login);

  /**
   * Finds a user by email (case insensitive).
   *
   * @param email the email
   * @return optional user
   */
  Optional<User> findByEmail(String email);

  /**
   * Finds a user by activation key.
   *
   * @param activationKey the activation key
   * @return optional user
   */
  Optional<User> findByActivationKey(String activationKey);

  /**
   * Finds a user by reset key.
   *
   * @param resetKey the reset key
   * @return optional user
   */
  Optional<User> findByResetKey(String resetKey);

  /**
   * Finds a user with authorities by login.
   *
   * @param login the login
   * @return optional user with authorities loaded
   */
  Optional<User> findWithAuthoritiesByLogin(String login);

  /**
   * Finds a user with authorities by email.
   *
   * @param email the email
   * @return optional user with authorities loaded
   */
  Optional<User> findWithAuthoritiesByEmail(String email);

  /**
   * Finds all activated users with pagination.
   *
   * @param pageable pagination information
   * @return page of activated users
   */
  Page<User> findAllActivated(Pageable pageable);

  /**
   * Finds all users with pagination.
   *
   * @param pageable pagination information
   * @return page of users
   */
  Page<User> findAll(Pageable pageable);

  /**
   * Finds all non-activated users created before the given date.
   *
   * @param dateTime the cutoff date
   * @return list of non-activated users
   */
  List<User> findAllNonActivatedBefore(Instant dateTime);

  /**
   * Deletes a user.
   *
   * @param user the user to delete
   */
  void delete(User user);

  /**
   * Checks if a user exists by login.
   *
   * @param login the login
   * @return true if user exists
   */
  boolean existsByLogin(String login);

  /**
   * Checks if a user exists by email.
   *
   * @param email the email
   * @return true if user exists
   */
  boolean existsByEmail(String email);
}
