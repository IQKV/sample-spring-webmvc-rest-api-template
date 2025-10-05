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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.UserDomainRepository;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements the domain repository interface using Spring Data JPA.
 * This class bridges the domain layer with the infrastructure layer.
 */
@Component
public class UserRepositoryAdapter implements UserDomainRepository {

  private final UserRepository userRepository;
  private final CacheManager cacheManager;

  public UserRepositoryAdapter(UserRepository userRepository, CacheManager cacheManager) {
    this.userRepository = userRepository;
    this.cacheManager = cacheManager;
  }

  @Override
  public User save(User user) {
    User savedUser = userRepository.save(user);
    clearUserCaches(savedUser);
    return savedUser;
  }

  @Override
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  public Optional<User> findByLogin(String login) {
    return userRepository.findOneByLogin(login.toLowerCase());
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findOneByEmailIgnoreCase(email);
  }

  @Override
  public Optional<User> findByActivationKey(String activationKey) {
    return userRepository.findOneByActivationKey(activationKey);
  }

  @Override
  public Optional<User> findByResetKey(String resetKey) {
    return userRepository.findOneByResetKey(resetKey);
  }

  @Override
  public Optional<User> findWithAuthoritiesByLogin(String login) {
    return userRepository.findOneWithAuthoritiesByLogin(login.toLowerCase());
  }

  @Override
  public Optional<User> findWithAuthoritiesByEmail(String email) {
    return userRepository.findOneWithAuthoritiesByEmailIgnoreCase(email);
  }

  @Override
  public Page<User> findAllActivated(Pageable pageable) {
    return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable);
  }

  @Override
  public Page<User> findAll(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public List<User> findAllNonActivatedBefore(Instant dateTime) {
    return userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(dateTime);
  }

  @Override
  public void delete(User user) {
    userRepository.delete(user);
    clearUserCaches(user);
  }

  @Override
  public boolean existsByLogin(String login) {
    return userRepository.existsByLogin(login.toLowerCase());
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmailIgnoreCase(email);
  }

  private void clearUserCaches(User user) {
    if (cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE) != null) {
      cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).evictIfPresent(user.getLogin());
    }
    if (user.getEmail() != null && cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE) != null) {
      cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE).evictIfPresent(user.getEmail());
    }
  }
}
