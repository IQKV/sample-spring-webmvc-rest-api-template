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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.Authority;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.UserDomainRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.UserDomainService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.AuthorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for UserManagementService.
 */
@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

  @Mock
  private UserDomainRepository userRepository;

  @Mock
  private UserDomainService userDomainService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthorityRepository authorityRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private UserManagementService userManagementService;

  @BeforeEach
  void setUp() {
    userManagementService = new UserManagementService(
        userRepository,
        userDomainService,
        passwordEncoder,
        authorityRepository,
        eventPublisher
    );
  }

  @Test
  void shouldActivateRegistration() {
    // Given
    String activationKey = "activation-key";
    User user = createTestUser();
    user.setActivationKey(activationKey);
    user.setActivated(false);

    when(userRepository.findByActivationKey(activationKey)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // When
    Optional<User> result = userManagementService.activateRegistration(activationKey);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().isActivated()).isTrue();
    assertThat(result.get().getActivationKey()).isNull();

    // Verify event is published
    ArgumentCaptor<UserActivatedEvent> eventCaptor = ArgumentCaptor.forClass(UserActivatedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    UserActivatedEvent event = eventCaptor.getValue();
    assertThat(event.userId()).isEqualTo(user.getId());
    assertThat(event.login()).isEqualTo(user.getLogin());
    assertThat(event.email()).isEqualTo(user.getEmail());
  }

  @Test
  void shouldReturnEmptyWhenActivationKeyNotFound() {
    // Given
    String activationKey = "non-existent-key";
    when(userRepository.findByActivationKey(activationKey)).thenReturn(Optional.empty());

    // When
    Optional<User> result = userManagementService.activateRegistration(activationKey);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldCompletePasswordReset() {
    // Given
    String resetKey = "reset-key";
    String newPassword = "new-password";
    String encodedPassword = "encoded-password";

    User user = createTestUser();
    user.setResetKey(resetKey);
    user.setResetDate(Instant.now());

    when(userRepository.findByResetKey(resetKey)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class))).thenReturn(user);

    // When
    Optional<User> result = userManagementService.completePasswordReset(newPassword, resetKey);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getPassword()).isEqualTo(encodedPassword);
    assertThat(result.get().getResetKey()).isNull();
    assertThat(result.get().getResetDate()).isNull();
  }

  @Test
  void shouldRegisterUser() {
    // Given
    AdminUserDTO userDTO = createTestUserDTO();
    String password = "password";
    String encodedPassword = "encoded-password";

    Authority userAuthority = new Authority();
    userAuthority.setName(AuthoritiesConstants.USER);

    when(userRepository.findByLogin(anyString())).thenReturn(Optional.empty());
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
    when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(Optional.of(userAuthority));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(1L);
      return user;
    });

    // When
    User result = userManagementService.registerUser(userDTO, password);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getLogin()).isEqualTo(userDTO.getLogin().toLowerCase());
    assertThat(result.getEmail()).isEqualTo(userDTO.getEmail().toLowerCase());
    assertThat(result.getPassword()).isEqualTo(encodedPassword);
    assertThat(result.isActivated()).isFalse();
    assertThat(result.getActivationKey()).isNotNull();

    // Verify event is published
    ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    UserRegisteredEvent event = eventCaptor.getValue();
    assertThat(event.userId()).isEqualTo(result.getId());
    assertThat(event.login()).isEqualTo(result.getLogin());
    assertThat(event.email()).isEqualTo(result.getEmail());
  }

  @Test
  void shouldThrowExceptionWhenRegisteringUserWithExistingLogin() {
    // Given
    AdminUserDTO userDTO = createTestUserDTO();
    String password = "password";

    User existingUser = createTestUser();
    existingUser.setActivated(true); // Activated user cannot be removed

    when(userRepository.findByLogin(userDTO.getLogin().toLowerCase())).thenReturn(Optional.of(existingUser));

    // When & Then
    assertThatThrownBy(() -> userManagementService.registerUser(userDTO, password))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Login already exists: " + userDTO.getLogin().toLowerCase());
  }

  @Test
  void shouldCreateUser() {
    // Given
    AdminUserDTO userDTO = createTestUserDTO();
    String encodedPassword = "encoded-password";

    when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(1L);
      return user;
    });

    // When
    User result = userManagementService.createUser(userDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getLogin()).isEqualTo(userDTO.getLogin().toLowerCase());
    assertThat(result.getEmail()).isEqualTo(userDTO.getEmail().toLowerCase());
    assertThat(result.isActivated()).isTrue();
    assertThat(result.getResetKey()).isNotNull();

    // Verify domain service validation was called
    verify(userDomainService).validateUniqueLogin(userDTO.getLogin().toLowerCase());
    verify(userDomainService).validateUniqueEmail(userDTO.getEmail().toLowerCase());
  }

  private User createTestUser() {
    User user = new User();
    user.setId(1L);
    user.setLogin("testuser");
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setActivated(true);
    return user;
  }

  @Test
  void shouldRequestPasswordReset() {
    // Given
    User user = createTestUser();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // When
    Optional<User> result = userManagementService.requestPasswordReset(user.getEmail());

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getResetKey()).isNotNull();
    assertThat(result.get().getResetDate()).isNotNull();
  }

  @Test
  void shouldReturnEmptyWhenRequestingPasswordResetForNonExistentUser() {
    // Given
    String email = "nonexistent@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    Optional<User> result = userManagementService.requestPasswordReset(email);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldNotAllowPasswordResetForInactiveUser() {
    // Given
    User user = createTestUser();
    user.setActivated(false);
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    // When
    Optional<User> result = userManagementService.requestPasswordReset(user.getEmail());

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldRemoveNotActivatedUsers() {
    // Given
    Instant threeDaysAgo = Instant.now().minus(3, java.time.Duration.ofDays(3));
    List<User> inactiveUsers = List.of(createTestUser(), createTestUser());

    when(userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(any(Instant.class)))
        .thenReturn(inactiveUsers);

    // When
    userManagementService.removeNotActivatedUsers();

    // Then
    verify(userRepository).deleteAll(inactiveUsers);
  }

  @Test
  void shouldUpdateUserFromDTO() {
    // Given
    AdminUserDTO userDTO = createTestUserDTO();
    userDTO.setId(1L);

    User existingUser = createTestUser();
    existingUser.setId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);

    // When
    Optional<User> result = userManagementService.updateUser(userDTO);

    // Then
    assertThat(result).isPresent();
    verify(userDomainService).validateUniqueEmail(userDTO.getEmail().toLowerCase());
  }

  private AdminUserDTO createTestUserDTO() {
    AdminUserDTO userDTO = new AdminUserDTO();
    userDTO.setLogin("testuser");
    userDTO.setEmail("test@example.com");
    userDTO.setFirstName("Test");
    userDTO.setLastName("User");
    userDTO.setLangKey("en");
    userDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));
    return userDTO;
  }
}
