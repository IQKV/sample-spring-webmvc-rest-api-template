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

package com.iqkv.sample.webmvc.dashboard.user.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iqkv.sample.webmvc.dashboard.user.domain.User;
import com.iqkv.sample.webmvc.dashboard.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Create a new user.
   *
   * @param login     the login
   * @param firstName the first name
   * @param lastName  the last name
   * @param email     the email
   * @return the created user
   */
  public User createUser(String login, String firstName, String lastName, String email) {
    User user = new User(login, firstName, lastName, email);
    User savedUser = userRepository.save(user);
    log.debug("Created user: {}", savedUser);
    return savedUser;
  }

  /**
   * Get all users.
   *
   * @param pageable the pagination information
   * @return the list of users
   */
  @Transactional(readOnly = true)
  public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  /**
   * Get user by login.
   *
   * @param login the login
   * @return the user
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserByLogin(String login) {
    return userRepository.findOneByLogin(login);
  }

  /**
   * Delete user by login.
   *
   * @param login the login
   */
  public void deleteUser(String login) {
    userRepository.findOneByLogin(login).ifPresent(user -> {
      userRepository.delete(user);
      log.debug("Deleted user: {}", user);
    });
  }
}