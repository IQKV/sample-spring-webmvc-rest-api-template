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
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for UserManagementController within the User Management bounded context.
 * <p>
 * This test class focuses on testing the presentation layer of the User Management bounded context,
 * ensuring proper HTTP API behavior, request/response handling, and integration with the application layer.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class UserManagementControllerIT {

  private static final String DEFAULT_LOGIN = "johndoe";
  private static final String UPDATED_LOGIN = "knowhow";
  private static final Long DEFAULT_ID = 1L;
  private static final String DEFAULT_EMAIL = "johndoe@localhost";
  private static final String UPDATED_EMAIL = "knowhow@localhost";
  private static final String DEFAULT_FIRSTNAME = "john";
  private static final String UPDATED_FIRSTNAME = "knowhowFirstName";
  private static final String DEFAULT_LASTNAME = "doe";
  private static final String UPDATED_LASTNAME = "knowhowLastName";
  private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
  private static final String UPDATED_IMAGEURL = "http://placehold.it/40x40";
  private static final String DEFAULT_LANGKEY = "en";
  private static final String UPDATED_LANGKEY = "fr";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private CacheManager cacheManager;

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
    clearCaches();
    cleanupTestUsers();
    assertThat(userRepository.count()).isEqualTo(initialUserCount);
  }

  @Test
  @Transactional
  void shouldCreateUserSuccessfully() throws Exception {
    // Given
    UserDTO userDTO = createUserDTO();

    // When & Then
    var response = mockMvc
        .perform(post("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userDTO)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    UserDTO returnedUser = objectMapper.readValue(response, UserDTO.class);

    // Verify response
    assertThat(returnedUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
    assertThat(returnedUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
    assertThat(returnedUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
    assertThat(returnedUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
    assertThat(returnedUser.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
    assertThat(returnedUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
  }

  @Test
  @Transactional
  void shouldRejectUserCreationWithExistingId() throws Exception {
    // Given
    UserDTO userDTO = createUserDTO();
    userDTO.setId(DEFAULT_ID);

    // When & Then
    mockMvc
        .perform(post("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldRejectUserCreationWithExistingLogin() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);
    UserDTO userDTO = createUserDTO();
    userDTO.setLogin(DEFAULT_LOGIN); // Same as existing user

    // When & Then
    mockMvc
        .perform(post("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldRejectUserCreationWithExistingEmail() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);
    UserDTO userDTO = createUserDTO();
    userDTO.setLogin("anotherlogin");
    userDTO.setEmail(DEFAULT_EMAIL); // Same as existing user

    // When & Then
    mockMvc
        .perform(post("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(userDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  void shouldRetrieveAllUsers() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/admin/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
        .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
        .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
        .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
        .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGEURL)))
        .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
  }

  @Test
  @Transactional
  void shouldRetrieveUserByLogin() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(get("/api/admin/users/{login}", testUser.getLogin()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.login").value(testUser.getLogin()))
        .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
        .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
        .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
        .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGEURL))
        .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));
  }

  @Test
  @Transactional
  void shouldReturnNotFoundForNonExistentUser() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/admin/users/unknown"))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  void shouldUpdateUserSuccessfully() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);
    User savedUser = userRepository.findById(testUser.getId()).orElseThrow();

    UserDTO updateDTO = new UserDTO();
    updateDTO.setId(savedUser.getId());
    updateDTO.setLogin(savedUser.getLogin());
    updateDTO.setFirstName(UPDATED_FIRSTNAME);
    updateDTO.setLastName(UPDATED_LASTNAME);
    updateDTO.setEmail(UPDATED_EMAIL);
    updateDTO.setActivated(savedUser.isActivated());
    updateDTO.setImageUrl(UPDATED_IMAGEURL);
    updateDTO.setLangKey(UPDATED_LANGKEY);
    updateDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

    // When & Then
    mockMvc
        .perform(put("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(updateDTO)))
        .andExpect(status().isOk());

    // Verify database state
    User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
    assertThat(updatedUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
    assertThat(updatedUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
    assertThat(updatedUser.getEmail()).isEqualTo(UPDATED_EMAIL);
    assertThat(updatedUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
    assertThat(updatedUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
  }

  @Test
  @Transactional
  void shouldDeleteUserSuccessfully() throws Exception {
    // Given
    userRepository.saveAndFlush(testUser);

    // When & Then
    mockMvc
        .perform(delete("/api/admin/users/{login}", testUser.getLogin())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Verify user is deleted
    assertThat(userRepository.findByLogin(testUser.getLogin())).isEmpty();
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

  private UserDTO createUserDTO() {
    UserDTO userDTO = new UserDTO();
    userDTO.setLogin(DEFAULT_LOGIN);
    userDTO.setFirstName(DEFAULT_FIRSTNAME);
    userDTO.setLastName(DEFAULT_LASTNAME);
    userDTO.setEmail(DEFAULT_EMAIL);
    userDTO.setActivated(true);
    userDTO.setImageUrl(DEFAULT_IMAGEURL);
    userDTO.setLangKey(DEFAULT_LANGKEY);
    userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
    return userDTO;
  }

  private void clearCaches() {
    cacheManager.getCacheNames()
        .stream()
        .map(cacheManager::getCache)
        .filter(Objects::nonNull)
        .forEach(Cache::clear);
  }

  private void cleanupTestUsers() {
    userRepository.findByLogin(DEFAULT_LOGIN).ifPresent(userRepository::delete);
    userRepository.findByLogin(UPDATED_LOGIN).ifPresent(userRepository::delete);
    userRepository.findByLogin(testUser.getLogin()).ifPresent(userRepository::delete);
    userRepository.findByLogin("anotherlogin").ifPresent(userRepository::delete);
  }
}
