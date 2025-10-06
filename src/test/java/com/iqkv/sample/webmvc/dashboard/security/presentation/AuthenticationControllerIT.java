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

package com.iqkv.sample.webmvc.dashboard.security.presentation;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.security.presentation.vm.LoginVM;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.BoundedContextTestBase;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AuthenticationController within the Security bounded context.
 * <p>
 * Tests the presentation layer of the authentication system, including
 * JWT token generation, login validation, and security event tracking.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AuthenticationControllerIT extends BoundedContextTestBase {

  private static final String TEST_USER_LOGIN = "user-jwt-controller";
  private static final String TEST_USER_EMAIL = "user-jwt-controller@example.com";
  private static final String TEST_PASSWORD = "test";
  private static final String REMEMBER_ME_USER_LOGIN = "user-jwt-controller-remember-me";
  private static final String REMEMBER_ME_USER_EMAIL = "user-jwt-controller-remember-me@example.com";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MockMvc mockMvc;

  @AfterEach
  void tearDown() {
    super.tearDown();
    cleanupBoundedContextData();
  }

  @Test
  @Transactional
  void shouldAuthenticateValidUser() throws Exception {
    // Given
    User user = createTestUser(TEST_USER_LOGIN, TEST_USER_EMAIL);
    userRepository.saveAndFlush(user);

    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername(TEST_USER_LOGIN);
    loginRequest.setPassword(TEST_PASSWORD);

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id_token").isString())
        .andExpect(jsonPath("$.id_token").isNotEmpty())
        .andExpect(header().string("Authorization", not(nullValue())))
        .andExpect(header().string("Authorization", not(is(emptyString()))));
  }

  @Test
  @Transactional
  void shouldAuthenticateWithRememberMe() throws Exception {
    // Given
    User user = createTestUser(REMEMBER_ME_USER_LOGIN, REMEMBER_ME_USER_EMAIL);
    userRepository.saveAndFlush(user);

    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername(REMEMBER_ME_USER_LOGIN);
    loginRequest.setPassword(TEST_PASSWORD);
    loginRequest.setRememberMe(true);

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id_token").isString())
        .andExpect(jsonPath("$.id_token").isNotEmpty())
        .andExpect(header().string("Authorization", not(nullValue())))
        .andExpect(header().string("Authorization", not(is(emptyString()))));
  }

  @Test
  @Transactional
  void shouldRejectInvalidCredentials() throws Exception {
    // Given
    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername("wrong-user");
    loginRequest.setPassword("wrong-password");

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.id_token").doesNotExist())
        .andExpect(header().doesNotExist("Authorization"));
  }

  @Test
  @Transactional
  void shouldRejectInactiveUser() throws Exception {
    // Given
    User inactiveUser = createTestUser("inactive-user", "inactive@example.com");
    inactiveUser.setActivated(false);
    userRepository.saveAndFlush(inactiveUser);

    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername("inactive-user");
    loginRequest.setPassword(TEST_PASSWORD);

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.id_token").doesNotExist())
        .andExpect(header().doesNotExist("Authorization"));
  }

  @Test
  @Transactional
  void shouldRejectEmptyCredentials() throws Exception {
    // Given
    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername("");
    loginRequest.setPassword("");

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldRejectNullCredentials() throws Exception {
    // Given
    LoginVM loginRequest = new LoginVM();
    // username and password are null by default

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldAuthenticateByEmail() throws Exception {
    // Given
    User user = createTestUser(TEST_USER_LOGIN, TEST_USER_EMAIL);
    userRepository.saveAndFlush(user);

    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername(TEST_USER_EMAIL); // Using email instead of login
    loginRequest.setPassword(TEST_PASSWORD);

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id_token").isString())
        .andExpect(jsonPath("$.id_token").isNotEmpty())
        .andExpect(header().string("Authorization", not(nullValue())));
  }

  @Test
  @Transactional
  void shouldHandleCaseInsensitiveLogin() throws Exception {
    // Given
    User user = createTestUser(TEST_USER_LOGIN, TEST_USER_EMAIL);
    userRepository.saveAndFlush(user);

    LoginVM loginRequest = new LoginVM();
    loginRequest.setUsername(TEST_USER_LOGIN.toUpperCase()); // Case insensitive
    loginRequest.setPassword(TEST_PASSWORD);

    // When & Then
    mockMvc
        .perform(post("/api/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id_token").isString())
        .andExpect(jsonPath("$.id_token").isNotEmpty());
  }

  @Override
  protected void cleanupBoundedContextData() {
    userRepository.findByLogin(TEST_USER_LOGIN).ifPresent(userRepository::delete);
    userRepository.findByLogin(REMEMBER_ME_USER_LOGIN).ifPresent(userRepository::delete);
    userRepository.findByLogin("inactive-user").ifPresent(userRepository::delete);
  }

  private User createTestUser(String login, String email) {
    User user = new User();
    user.setLogin(login);
    user.setEmail(email);
    user.setActivated(true);
    user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
    user.setFirstName("Test");
    user.setLastName("User");
    user.setLangKey("en");
    return user;
  }
}
