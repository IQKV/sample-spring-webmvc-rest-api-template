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

package com.iqkv.sample.webmvc.dashboard.usermanagement.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.presentation.vm.PasswordChangeVM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AccountController within the User Management bounded context.
 * <p>
 * Tests account-related operations like profile management, password changes,
 * and account activation within the user management domain.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AccountControllerIT {

  private static final String TEST_USER_LOGIN = "test-account-user";
  private static final String TEST_USER_EMAIL = "test-account@localhost";
  private static final String TEST_USER_FIRSTNAME = "Test";
  private static final String TEST_USER_LASTNAME = "Account";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MockMvc mockMvc;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = createTestUser();
  }

  @AfterEach
  void tearDown() {
    userRepository.findByLogin(TEST_USER_LOGIN).ifPresent(userRepository::delete);
  }

  @Test
  @WithMockUser(username = TEST_USER_LOGIN)
  @Transactional
  void shouldGetCurrentUserAccount() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.login").value(TEST_USER_LOGIN))
        .andExpect(jsonPath("$.firstName").value(TEST_USER_FIRSTNAME))
        .andExpect(jsonPath("$.lastName").value(TEST_USER_LASTNAME))
        .andExpect(jsonPath("$.email").value(TEST_USER_EMAIL))
        .andExpect(jsonPath("$.activated").value(true));
  }

  @Test
  void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/account"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = TEST_USER_LOGIN)
  @Transactional
  void shouldUpdateUserAccount() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    UserDTO updatedAccount = new UserDTO();
    updatedAccount.setLogin(TEST_USER_LOGIN);
    updatedAccount.setFirstName("Updated");
    updatedAccount.setLastName("Name");
    updatedAccount.setEmail("updated@localhost");
    updatedAccount.setLangKey("fr");
    updatedAccount.setImageUrl("http://example.com/updated.jpg");

    // When & Then
    mockMvc
        .perform(post("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(updatedAccount)))
        .andExpect(status().isOk());

    // Verify database state
    User savedUser = userRepository.findByLogin(TEST_USER_LOGIN).orElseThrow();
    assertThat(savedUser.getFirstName()).isEqualTo("Updated");
    assertThat(savedUser.getLastName()).isEqualTo("Name");
    assertThat(savedUser.getEmail()).isEqualTo("updated@localhost");
    assertThat(savedUser.getLangKey()).isEqualTo("fr");
    assertThat(savedUser.getImageUrl()).isEqualTo("http://example.com/updated.jpg");
  }

  @Test
  @WithMockUser(username = TEST_USER_LOGIN)
  @Transactional
  void shouldChangePassword() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    PasswordChangeVM passwordChange = new PasswordChangeVM();
    passwordChange.setCurrentPassword("current-password");
    passwordChange.setNewPassword("new-password");

    // When & Then
    mockMvc
        .perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(passwordChange)))
        .andExpect(status().isOk());

    // Verify password was changed
    User savedUser = userRepository.findByLogin(TEST_USER_LOGIN).orElseThrow();
    assertThat(passwordEncoder.matches("new-password", savedUser.getPassword())).isTrue();
  }

  @Test
  @WithMockUser(username = TEST_USER_LOGIN)
  @Transactional
  void shouldRejectPasswordChangeWithWrongCurrentPassword() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    PasswordChangeVM passwordChange = new PasswordChangeVM();
    passwordChange.setCurrentPassword("wrong-password");
    passwordChange.setNewPassword("new-password");

    // When & Then
    mockMvc
        .perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(passwordChange)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldActivateAccount() throws Exception {
    // Given
    testUser.setActivated(false);
    testUser.setActivationKey("activation-key-123");
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/activate?key=activation-key-123"))
        .andExpect(status().isOk());

    // Verify account is activated
    User activatedUser = userRepository.findByLogin(TEST_USER_LOGIN).orElseThrow();
    assertThat(activatedUser.isActivated()).isTrue();
    assertThat(activatedUser.getActivationKey()).isNull();
  }

  @Test
  @Transactional
  void shouldReturnBadRequestForInvalidActivationKey() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/activate?key=invalid-key"))
        .andExpect(status().isBadRequest());
  }

  private User createTestUser() {
    User user = new User();
    user.setLogin(TEST_USER_LOGIN);
    user.setPassword(passwordEncoder.encode("current-password"));
    user.setActivated(true);
    user.setEmail(TEST_USER_EMAIL);
    user.setFirstName(TEST_USER_FIRSTNAME);
    user.setLastName(TEST_USER_LASTNAME);
    user.setImageUrl("http://example.com/image.jpg");
    user.setLangKey("en");
    return user;
  }
}
