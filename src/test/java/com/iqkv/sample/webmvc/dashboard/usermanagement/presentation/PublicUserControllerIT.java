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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.BoundedContextTestBase;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for PublicUserController within the User Management bounded context.
 * <p>
 * Tests public user API endpoints that expose limited user information
 * without sensitive data like email addresses or personal details.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class PublicUserControllerIT extends BoundedContextTestBase {

  private static final String DEFAULT_LOGIN = "johndoe";
  private static final String DEFAULT_EMAIL = "johndoe@localhost";
  private static final String DEFAULT_FIRSTNAME = "john";
  private static final String DEFAULT_LASTNAME = "doe";
  private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
  private static final String DEFAULT_LANGKEY = "en";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MockMvc mockMvc;

  private User testUser;
  private Long initialUserCount;

  @BeforeEach
  void setUp() {
    initialUserCount = userRepository.count();
    testUser = createTestUser();
  }

  @AfterEach
  void tearDown() {
    super.tearDown();
    cleanupBoundedContextData();
    assertThat(userRepository.count()).isEqualTo(initialUserCount);
  }

  @Test
  @Transactional
  void shouldGetAllPublicUsers() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[?(@.id == %d)].login", testUser.getId()).value(testUser.getLogin()))
        .andExpect(jsonPath("$.[?(@.id == %d)].keys()", testUser.getId()).value(Set.of("id", "login")))
        .andExpect(jsonPath("$.[*].email").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].imageUrl").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].langKey").doesNotHaveJsonPath());
  }

  @Test
  @Transactional
  void shouldRejectSortingBySensitiveFields() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then - Should reject sorting by sensitive fields
    mockMvc
        .perform(get("/api/users?sort=resetKey,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/api/users?sort=password,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/api/users?sort=email,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldRejectMultipleSortParametersWithSensitiveFields() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then - Should reject multiple sort parameters containing sensitive fields
    mockMvc
        .perform(get("/api/users?sort=resetKey,desc&sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldAllowSortingByAllowedFields() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then - Should allow sorting by safe fields
    mockMvc
        .perform(get("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/users?sort=login,asc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @Transactional
  void shouldReturnOnlyPublicUserFields() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/users")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[*].id").exists())
        .andExpect(jsonPath("$.[*].login").exists())
        // Verify sensitive fields are not exposed
        .andExpect(jsonPath("$.[*].email").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].password").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].resetKey").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].resetDate").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].activationKey").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].authorities").doesNotHaveJsonPath());
  }

  @Test
  @Transactional
  void shouldHandlePaginationCorrectly() throws Exception {
    // Given - Create multiple users
    for (int i = 0; i < 5; i++) {
      User user = createTestUser();
      user.setLogin("user" + i);
      user.setEmail("user" + i + "@localhost");
      userRepository.saveAndFlush(user);
    }

    // When & Then - Test pagination
    mockMvc
        .perform(get("/api/users?page=0&size=3")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()").value(3));
  }

  @Override
  protected void cleanupBoundedContextData() {
    userRepository.findByLogin(testUser.getLogin()).ifPresent(userRepository::delete);
    // Clean up any additional test users
    for (int i = 0; i < 5; i++) {
      userRepository.findByLogin("user" + i).ifPresent(userRepository::delete);
    }
  }

  private User createTestUser() {
    User user = new User();
    user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
    user.setPassword(RandomStringUtils.randomAlphanumeric(60));
    user.setActivated(true);
    user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
    user.setFirstName(DEFAULT_FIRSTNAME);
    user.setLastName(DEFAULT_LASTNAME);
    user.setImageUrl(DEFAULT_IMAGEURL);
    user.setLangKey(DEFAULT_LANGKEY);
    return user;
  }
}
