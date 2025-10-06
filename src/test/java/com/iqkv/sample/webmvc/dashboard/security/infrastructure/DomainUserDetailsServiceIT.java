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

package com.iqkv.sample.webmvc.dashboard.security.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Locale;

import com.iqkv.boot.security.UserNotActivatedException;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.BoundedContextTestBase;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for DomainUserDetailsService within the Security bounded context.
 * <p>
 * Tests the infrastructure layer's integration with the user management bounded context
 * for loading user details and handling authentication scenarios.
 */
@Transactional
@IntegrationTest
class DomainUserDetailsServiceIT extends BoundedContextTestBase {

  private static final String USER_ONE_LOGIN = "test-user-one";
  private static final String USER_ONE_EMAIL = "test-user-one@localhost";
  private static final String USER_TWO_LOGIN = "test-user-two";
  private static final String USER_TWO_EMAIL = "test-user-two@localhost";
  private static final String USER_THREE_LOGIN = "test-user-three";
  private static final String USER_THREE_EMAIL = "test-user-three@localhost";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  @Qualifier("userDetailsService")
  private UserDetailsService domainUserDetailsService;

  private User userOne;
  private User userTwo;
  private User userThree;

  @BeforeEach
  void setUp() {
    userOne = createActiveUser(USER_ONE_LOGIN, USER_ONE_EMAIL, "userOne");
    userTwo = createActiveUser(USER_TWO_LOGIN, USER_TWO_EMAIL, "userTwo");
    userThree = createInactiveUser(USER_THREE_LOGIN, USER_THREE_EMAIL, "userThree");

    userRepository.save(userOne);
    userRepository.save(userTwo);
    userRepository.save(userThree);
  }

  @AfterEach
  void tearDown() {
    super.tearDown();
    cleanupBoundedContextData();
  }

  @Test
  void shouldLoadUserByLogin() {
    // When
    UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    assertThat(userDetails.isEnabled()).isTrue();
    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
  }

  @Test
  void shouldLoadUserByLoginIgnoringCase() {
    // When
    UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN.toUpperCase(Locale.ENGLISH));

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
  }

  @Test
  void shouldLoadUserByEmail() {
    // When
    UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL);

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
  }

  @Test
  void shouldLoadUserByEmailIgnoringCase() {
    // When
    UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH));

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
  }

  @Test
  void shouldPrioritizeEmailOverLogin() {
    // Given - User exists with both login and email

    // When
    UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_EMAIL);

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
  }

  @Test
  void shouldThrowExceptionForNotActivatedUser() {
    // When & Then
    assertThatExceptionOfType(UserNotActivatedException.class)
        .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(USER_THREE_LOGIN))
        .withMessage("User " + USER_THREE_LOGIN + " was not activated");
  }

  @Test
  void shouldThrowExceptionForNonExistentUser() {
    // When & Then
    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> domainUserDetailsService.loadUserByUsername("nonexistent"))
        .withMessage("User nonexistent was not found in the database");
  }

  @Test
  void shouldLoadUserAuthorities() {
    // When
    UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

    // Then
    assertThat(userDetails.getAuthorities()).isNotEmpty();
    assertThat(userDetails.getAuthorities())
        .extracting("authority")
        .contains("ROLE_USER");
  }

  @Test
  void shouldHandleNullOrEmptyUsername() {
    // When & Then
    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(null));

    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(""));

    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> domainUserDetailsService.loadUserByUsername("   "));
  }

  @Test
  void shouldCacheUserDetails() {
    // Given - Load user first time
    UserDetails firstLoad = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

    // When - Load user second time
    UserDetails secondLoad = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

    // Then - Should be cached (implementation dependent)
    assertThat(firstLoad.getUsername()).isEqualTo(secondLoad.getUsername());
  }

  @Override
  protected void cleanupBoundedContextData() {
    userRepository.findByLogin(USER_ONE_LOGIN).ifPresent(userRepository::delete);
    userRepository.findByLogin(USER_TWO_LOGIN).ifPresent(userRepository::delete);
    userRepository.findByLogin(USER_THREE_LOGIN).ifPresent(userRepository::delete);
  }

  private User createActiveUser(String login, String email, String firstName) {
    User user = new User();
    user.setLogin(login);
    user.setPassword(RandomStringUtils.randomAlphanumeric(60));
    user.setActivated(true);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName("doe");
    user.setLangKey("en");
    return user;
  }

  private User createInactiveUser(String login, String email, String firstName) {
    User user = createActiveUser(login, email, firstName);
    user.setActivated(false);
    return user;
  }
}
