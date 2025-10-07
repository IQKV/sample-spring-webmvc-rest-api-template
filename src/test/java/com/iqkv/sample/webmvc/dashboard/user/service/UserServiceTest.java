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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.iqkv.sample.webmvc.dashboard.user.domain.User;
import com.iqkv.sample.webmvc.dashboard.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository);
  }

  @Test
  void shouldCreateUser() {
    // Given
    User user = new User("testuser", "John", "Doe", "john@example.com");
    when(userRepository.save(any(User.class))).thenReturn(user);

    // When
    User result = userService.createUser("testuser", "John", "Doe", "john@example.com");

    // Then
    assertThat(result.getLogin()).isEqualTo("testuser");
    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getLastName()).isEqualTo("Doe");
    assertThat(result.getEmail()).isEqualTo("john@example.com");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldGetAllUsers() {
    // Given
    User user = new User("testuser", "John", "Doe", "john@example.com");
    Page<User> page = new PageImpl<>(List.of(user));
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

    // When
    Page<User> result = userService.getAllUsers(PageRequest.of(0, 10));

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getLogin()).isEqualTo("testuser");
  }

  @Test
  void shouldGetUserByLogin() {
    // Given
    User user = new User("testuser", "John", "Doe", "john@example.com");
    when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.of(user));

    // When
    Optional<User> result = userService.getUserByLogin("testuser");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getLogin()).isEqualTo("testuser");
  }

  @Test
  void shouldDeleteUser() {
    // Given
    User user = new User("testuser", "John", "Doe", "john@example.com");
    when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.of(user));

    // When
    userService.deleteUser("testuser");

    // Then
    verify(userRepository).delete(user);
  }
}