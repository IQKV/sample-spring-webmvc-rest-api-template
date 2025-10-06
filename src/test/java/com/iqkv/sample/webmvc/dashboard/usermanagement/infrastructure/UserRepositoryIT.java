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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration tests for UserRepository within the User Management bounded context.
 * <p>
 * Tests the infrastructure layer's data access patterns, custom queries,
 * and caching behavior specific to the user management domain.
 */
@DataJpaTest
@ContextConfiguration(classes = IntegrationTest.class)
class UserRepositoryIT {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CacheManager cacheManager;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = createTestUser();
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    clearCaches();
  }

  @Test
  void shouldFindUserByLogin() {
    // Given
    entityManager.persistAndFlush(testUser);

    // When
    Optional<User> foundUser = userRepository.findByLogin(testUser.getLogin());

    // Then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getLogin()).isEqualTo(testUser.getLogin());
    assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
  }

  @Test
  void shouldReturnEmptyWhenUserNotFoundByLogin() {
    // When
    Optional<User> foundUser = userRepository.findByLogin("nonexistent");

    // Then
    assertThat(foundUser).isEmpty();
  }

  @Test
  void shouldFindUserByEmail() {
    // Given
    entityManager.persistAndFlush(testUser);

    // When
    Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());

    // Then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
    assertThat(foundUser.get().getLogin()).isEqualTo(testUser.getLogin());
  }

  @Test
  void shouldFindUserByActivationKey() {
    // Given
    testUser.setActivationKey("activation-key-123");
    entityManager.persistAndFlush(testUser);

    // When
    Optional<User> foundUser = userRepository.findByActivationKey("activation-key-123");

    // Then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getActivationKey()).isEqualTo("activation-key-123");
  }

  @Test
  void shouldFindUserByResetKey() {
    // Given
    testUser.setResetKey("reset-key-123");
    testUser.setResetDate(Instant.now());
    entityManager.persistAndFlush(testUser);

    // When
    Optional<User> foundUser = userRepository.findByResetKey("reset-key-123");

    // Then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getResetKey()).isEqualTo("reset-key-123");
  }

  @Test
  void shouldFindAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore() {
    // Given
    User inactiveUser1 = createTestUser();
    inactiveUser1.setLogin("inactive1");
    inactiveUser1.setEmail("inactive1@test.com");
    inactiveUser1.setActivated(false);
    inactiveUser1.setActivationKey("key1");
    inactiveUser1.setCreatedDate(Instant.now().minus(4, ChronoUnit.DAYS));

    User inactiveUser2 = createTestUser();
    inactiveUser2.setLogin("inactive2");
    inactiveUser2.setEmail("inactive2@test.com");
    inactiveUser2.setActivated(false);
    inactiveUser2.setActivationKey("key2");
    inactiveUser2.setCreatedDate(Instant.now().minus(2, ChronoUnit.DAYS));

    User activeUser = createTestUser();
    activeUser.setLogin("active");
    activeUser.setEmail("active@test.com");
    activeUser.setActivated(true);
    activeUser.setCreatedDate(Instant.now().minus(4, ChronoUnit.DAYS));

    entityManager.persistAndFlush(inactiveUser1);
    entityManager.persistAndFlush(inactiveUser2);
    entityManager.persistAndFlush(activeUser);

    // When
    List<User> inactiveUsers = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
        Instant.now().minus(3, ChronoUnit.DAYS)
    );

    // Then
    assertThat(inactiveUsers).hasSize(1);
    assertThat(inactiveUsers.get(0).getLogin()).isEqualTo("inactive1");
  }

  @Test
  void shouldFindAllByActivatedIsFalseAndCreatedDateBefore() {
    // Given
    User inactiveUser1 = createTestUser();
    inactiveUser1.setLogin("inactive1");
    inactiveUser1.setEmail("inactive1@test.com");
    inactiveUser1.setActivated(false);
    inactiveUser1.setCreatedDate(Instant.now().minus(4, ChronoUnit.DAYS));

    User inactiveUser2 = createTestUser();
    inactiveUser2.setLogin("inactive2");
    inactiveUser2.setEmail("inactive2@test.com");
    inactiveUser2.setActivated(false);
    inactiveUser2.setCreatedDate(Instant.now().minus(2, ChronoUnit.DAYS));

    entityManager.persistAndFlush(inactiveUser1);
    entityManager.persistAndFlush(inactiveUser2);

    // When
    List<User> inactiveUsers = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(
        Instant.now().minus(3, ChronoUnit.DAYS)
    );

    // Then
    assertThat(inactiveUsers).hasSize(1);
    assertThat(inactiveUsers.get(0).getLogin()).isEqualTo("inactive1");
  }

  @Test
  void shouldCacheUserByLogin() {
    // Given
    entityManager.persistAndFlush(testUser);
    String cacheKey = testUser.getLogin();

    // When - First call should hit database
    Optional<User> firstCall = userRepository.findByLogin(cacheKey);

    // Then - Verify cache is populated
    assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(cacheKey)).isNotNull();

    // When - Second call should hit cache
    Optional<User> secondCall = userRepository.findByLogin(cacheKey);

    // Then
    assertThat(firstCall).isEqualTo(secondCall);
  }

  @Test
  void shouldEvictCacheOnUserUpdate() {
    // Given
    entityManager.persistAndFlush(testUser);
    String cacheKey = testUser.getLogin();

    // Populate cache
    userRepository.findByLogin(cacheKey);
    assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(cacheKey)).isNotNull();

    // When - Update user
    testUser.setFirstName("Updated");
    userRepository.saveAndFlush(testUser);

    // Then - Cache should be evicted
    assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(cacheKey)).isNull();
  }

  @Test
  void shouldEvictCacheOnUserDeletion() {
    // Given
    entityManager.persistAndFlush(testUser);
    String cacheKey = testUser.getLogin();

    // Populate cache
    userRepository.findByLogin(cacheKey);
    assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(cacheKey)).isNotNull();

    // When - Delete user
    userRepository.delete(testUser);

    // Then - Cache should be evicted
    assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(cacheKey)).isNull();
  }

  private User createTestUser() {
    User user = new User();
    user.setLogin("testuser");
    user.setPassword("password");
    user.setActivated(true);
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setImageUrl("http://example.com/image.jpg");
    user.setLangKey("en");
    return user;
  }

  private void clearCaches() {
    cacheManager.getCacheNames().forEach(cacheName ->
        cacheManager.getCache(cacheName).clear()
    );
  }
}
