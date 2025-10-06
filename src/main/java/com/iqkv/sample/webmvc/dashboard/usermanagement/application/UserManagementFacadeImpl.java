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

import com.iqkv.boot.security.SecurityUtils;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserManagementFacade providing anti-corruption layer
 * for other bounded contexts to interact with user management functionality.
 */
@Component
@Transactional(readOnly = true)
public class UserManagementFacadeImpl implements UserManagementFacade {

  private final UserRepository userRepository;

  public UserManagementFacadeImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserDTO> findUserById(Long id) {
    return userRepository.findById(id)
        .map(UserDTO::new);
  }

  @Override
  public Optional<UserDTO> findUserByLogin(String login) {
    return userRepository.findOneByLogin(login)
        .map(UserDTO::new);
  }

  @Override
  public boolean userExists(String login) {
    return userRepository.findOneByLogin(login).isPresent();
  }

  @Override
  public boolean userExistsByEmail(String email) {
    return userRepository.findOneByEmailIgnoreCase(email).isPresent();
  }

  @Override
  public Optional<UserDTO> getCurrentUser() {
    return SecurityUtils.getCurrentUserLogin()
        .flatMap(userRepository::findOneByLogin)
        .map(UserDTO::new);
  }

  @Override
  public boolean isUserActivated(String login) {
    return userRepository.findOneByLogin(login)
        .map(user -> user.isActivated())
        .orElse(false);
  }
}