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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.boot.security.RandomUtil;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for UserManagementService.
 */
@IntegrationTest
@Transactional
class UserManagementServiceIT {

  private static final String DEFAULT_LOGIN = "johndoe_service";
  private static final String DEFAULT_EMAIL = "johndoe_service@localhost";
  private static final String DEFAULT_FIRSTNAME = "john";
  private static final String DEFAULT_LASTNAME = "doe";
  private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
  private static final String DEFAULT_LANGKEY = "dummy";

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserManagementService userManagementService;

  @Autowired
  private AuditingHandler auditingHandler;

  @MockBean
  private DateTimeProvider dateTimeProvider;

  private User user;
  private Long numberOfUsers;

  @BeforeEach
  public void countUsers() {
    numberOfUsers = userRepository.count();
  }

  @BeforeEach
  public void init() {
    user = new User();
    user.setLogin(DEFAULT_LOGIN);
    user.setPassword(RandomStringUtils.randomAlphanumeric(60));
    user.setActivated(true);
    user.setEmail(DEFAULT_EMAIL);
    user.setFirstName(DEFAULT_FIRSTNAME);
    user.setLastName(DEFAULT_LASTNAME);
    user.setImageUrl(DEFAULT_IMAGEURL);
    user.setLangKey(DEFAULT_LANGKEY);

    when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.now()));
    auditingHandler.setDateTimeProvider(dateTimeProvider);
  }

  @AfterEach
  public void cleanupAndCheck() {
    cacheManager
        .getCacheNames()
        .stream()
        .map(cacheName -> this.cacheManager.getCache(cacheName))
        .filter(Objects::nonNull)
        .forEach(Cache::clear);
    userManagementService.deleteUser(DEFAULT_LOGIN);
    assertThat(userRepository.count()).isEqualTo(numberOfUsers);
    numberOfUsers = null;
  }

  @Test
  @Transactional
  void assertThatUserMustExistToResetPassword() {
    userRepository.saveAndFlush(user);
    Optional<User> maybeUser = userManagementService.requestPasswordReset("invalid.login@localhost");
    assertThat(maybeUser).isNotPresent();

    maybeUser = userManagementService.requestPasswordReset(user.getEmail());
    assertThat(maybeUser).isPresent();
    assertThat(maybeUser.orElse(null).getEmail()).isEqualTo(user.getEmail());
    assertThat(maybeUser.orElse(null).getResetDate()).isNotNull();
    assertThat(maybeUser.orElse(null).getResetKey()).isNotNull();
  }

  @Test
  @Transactional
  void assertThatOnlyActivatedUserCanRequestPasswordReset() {
    user.setActivated(false);
    userRepository.saveAndFlush(user);

    Optional<User> maybeUser = userManagementService.requestPasswordReset(user.getEmail());
    assertThat(maybeUser).isNotPresent();
    userRepository.delete(user);
  }

  @Test
  @Transactional
  void assertThatResetKeyMustNotBeOlderThan24Hours() {
    Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
    String resetKey = RandomUtil.generateResetKey();
    user.setActivated(true);
    user.setResetDate(daysAgo);
    user.setResetKey(resetKey);
    userRepository.saveAndFlush(user);

    Optional<User> maybeUser = userManagementService.completePasswordReset("johndoe2", user.getResetKey());
    assertThat(maybeUser).isNotPresent();
    userRepository.delete(user);
  }

  @Test
  @Transactional
  void assertThatResetKeyMustBeValid() {
    Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
    user.setActivated(true);
    user.setResetDate(daysAgo);
    user.setResetKey("1234");
    userRepository.saveAndFlush(user);

    Optional<User> maybeUser = userManagementService.completePasswordReset("johndoe2", user.getResetKey());
    assertThat(maybeUser).isNotPresent();
    userRepository.delete(user);
  }

  @Test
  @Transactional
  void assertThatUserCanResetPassword() {
    String oldPassword = user.getPassword();
    Instant daysAgo = Instant.now().minus(2, ChronoUnit.HOURS);
    String resetKey = RandomUtil.generateResetKey();
    user.setActivated(true);
    user.setResetDate(daysAgo);
    user.setResetKey(resetKey);
    userRepository.saveAndFlush(user);

    Optional<User> maybeUser = userManagementService.completePasswordReset("johndoe2", user.getResetKey());
    assertThat(maybeUser).isPresent();
    assertThat(maybeUser.orElse(null).getResetDate()).isNull();
    assertThat(maybeUser.orElse(null).getResetKey()).isNull();
    assertThat(maybeUser.orElse(null).getPassword()).isNotEqualTo(oldPassword);

    userRepository.delete(user);
  }

  @Test
  @Transactional
  void assertThatNotActivatedUsersWithNotNullActivationKeyCreatedBefore3DaysAreDeleted() {
    Instant now = Instant.now();
    when(dateTimeProvider.getNow()).thenReturn(Optional.of(now.minus(4, ChronoUnit.DAYS)));
    user.setActivated(false);
    user.setActivationKey(RandomStringUtils.random(20));
    User dbUser = userRepository.saveAndFlush(user);
    dbUser.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
    userRepository.saveAndFlush(user);
    Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
    List<User> users = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(threeDaysAgo);
    assertThat(users).isNotEmpty();
    userManagementService.removeNotActivatedUsers();
    users = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(threeDaysAgo);
    assertThat(users).isEmpty();
  }

  @Test
  @Transactional
  void assertThatNotActivatedUsersWithNullActivationKeyCreatedBefore3DaysAreNotDeleted() {
    Instant now = Instant.now();
    when(dateTimeProvider.getNow()).thenReturn(Optional.of(now.minus(4, ChronoUnit.DAYS)));
    user.setActivated(false);
    User dbUser = userRepository.saveAndFlush(user);
    dbUser.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
    userRepository.saveAndFlush(user);
    Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
    List<User> users = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(threeDaysAgo);
    assertThat(users).isEmpty();
    userManagementService.removeNotActivatedUsers();
    Optional<User> maybeDbUser = userRepository.findById(dbUser.getId());
    assertThat(maybeDbUser).contains(dbUser);
  }

  @Test
  @Transactional
  void shouldRegisterNewUser() {
    // Given
    AdminUserDTO userDTO = new AdminUserDTO();
    userDTO.setLogin("newuser");
    userDTO.setEmail("newuser@example.com");
    userDTO.setFirstName("New");
    userDTO.setLastName("User");
    userDTO.setLangKey("en");
    userDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));

    // When
    User registeredUser = userManagementService.registerUser(userDTO, "password");

    // Then
    assertThat(registeredUser).isNotNull();
    assertThat(registeredUser.getLogin()).isEqualTo("newuser");
    assertThat(registeredUser.getEmail()).isEqualTo("newuser@example.com");
    assertThat(registeredUser.isActivated()).isFalse();
    assertThat(registeredUser.getActivationKey()).isNotNull();

    // Cleanup
    userRepository.delete(registeredUser);
  }

  @Test
  @Transactional
  void shouldActivateUser() {
    // Given
    user.setActivated(false);
    user.setActivationKey("activation-key");
    userRepository.saveAndFlush(user);

    // When
    Optional<User> activatedUser = userManagementService.activateRegistration("activation-key");

    // Then
    assertThat(activatedUser).isPresent();
    assertThat(activatedUser.get().isActivated()).isTrue();
    assertThat(activatedUser.get().getActivationKey()).isNull();
  }

  @Test
  @Transactional
  void shouldCreateUserWithAuthorities() {
    // Given
    AdminUserDTO userDTO = new AdminUserDTO();
    userDTO.setLogin("adminuser");
    userDTO.setEmail("admin@example.com");
    userDTO.setFirstName("Admin");
    userDTO.setLastName("User");
    userDTO.setLangKey("en");
    userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER));

    // When
    User createdUser = userManagementService.createUser(userDTO);

    // Then
    assertThat(createdUser).isNotNull();
    assertThat(createdUser.getLogin()).isEqualTo("adminuser");
    assertThat(createdUser.isActivated()).isTrue();
    assertThat(createdUser.getAuthorities()).hasSize(2);

    // Cleanup
    userRepository.delete(createdUser);
  }
}
