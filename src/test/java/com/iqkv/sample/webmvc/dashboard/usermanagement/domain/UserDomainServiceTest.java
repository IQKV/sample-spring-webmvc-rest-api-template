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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for UserDomainService.
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

  @Mock
  private UserDomainRepository userRepository;

  private UserDomainService userDomainService;

  @BeforeEach
  void setUp() {
    userDomainService = new UserDomainService(userRepository);
  }

  @Test
  void shouldValidateUniqueLogin() {
    // Given
    String login = "testuser";
    when(userRepository.existsByLogin(login.toLowerCase())).thenReturn(false);

    // When & Then - Should not throw exception
    userDomainService.validateUniqueLogin(login);
  }

  @Test
  void shouldThrowExceptionWhenLoginAlreadyExists() {
    // Given
    String login = "existinguser";
    when(userRepository.existsByLogin(login.toLowerCase())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userDomainService.validateUniqueLogin(login))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Login already exists: " + login);
  }

  @Test
  void shouldValidateUniqueEmail() {
    // Given
    String email = "test@example.com";
    when(userRepository.existsByEmail(email.toLowerCase())).thenReturn(false);

    // When & Then - Should not throw exception
    userDomainService.validateUniqueEmail(email);
  }

  @Test
  void shouldThrowExceptionWhenEmailAlreadyExists() {
    // Given
    String email = "existing@example.com";
    when(userRepository.existsByEmail(email.toLowerCase())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userDomainService.validateUniqueEmail(email))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email already exists: " + email);
  }

  @Test
  void shouldValidateUniqueLoginForUser() {
    // Given
    String login = "testuser";
    Long userId = 1L;
    when(userRepository.findByLogin(login.toLowerCase())).thenReturn(Optional.empty());

    // When & Then - Should not throw exception
    userDomainService.validateUniqueLoginForUser(login, userId);
  }

  @Test
  void shouldValidateUniqueLoginForSameUser() {
    // Given
    String login = "testuser";
    Long userId = 1L;
    User existingUser = new User();
    existingUser.setId(userId);
    existingUser.setLogin(login);

    when(userRepository.findByLogin(login.toLowerCase())).thenReturn(Optional.of(existingUser));

    // When & Then - Should not throw exception (same user)
    userDomainService.validateUniqueLoginForUser(login, userId);
  }

  @Test
  void shouldThrowExceptionWhenLoginExistsForDifferentUser() {
    // Given
    String login = "testuser";
    Long userId = 1L;
    Long differentUserId = 2L;

    User existingUser = new User();
    existingUser.setId(differentUserId);
    existingUser.setLogin(login);

    when(userRepository.findByLogin(login.toLowerCase())).thenReturn(Optional.of(existingUser));

    // When & Then
    assertThatThrownBy(() -> userDomainService.validateUniqueLoginForUser(login, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Login already exists: " + login);
  }

  @Test
  void shouldValidateUniqueEmailForUser() {
    // Given
    String email = "test@example.com";
    Long userId = 1L;
    when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.empty());

    // When & Then - Should not throw exception
    userDomainService.validateUniqueEmailForUser(email, userId);
  }

  @Test
  void shouldValidateUniqueEmailForSameUser() {
    // Given
    String email = "test@example.com";
    Long userId = 1L;
    User existingUser = new User();
    existingUser.setId(userId);
    existingUser.setEmail(email);

    when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(existingUser));

    // When & Then - Should not throw exception (same user)
    userDomainService.validateUniqueEmailForUser(email, userId);
  }

  @Test
  void shouldThrowExceptionWhenEmailExistsForDifferentUser() {
    // Given
    String email = "test@example.com";
    Long userId = 1L;
    Long differentUserId = 2L;

    User existingUser = new User();
    existingUser.setId(differentUserId);
    existingUser.setEmail(email);

    when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(existingUser));

    // When & Then
    assertThatThrownBy(() -> userDomainService.validateUniqueEmailForUser(email, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email already exists: " + email);
  }
}
