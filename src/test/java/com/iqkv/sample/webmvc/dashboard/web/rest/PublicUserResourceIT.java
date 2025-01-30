/*
 * Copyright 2025 IQKV Team.
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

package com.iqkv.sample.webmvc.dashboard.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.Set;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.domain.User;
import com.iqkv.sample.webmvc.dashboard.repository.UserRepository;
import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.service.UserService;
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
 * Integration tests for the {@link PublicUserResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class PublicUserResourceIT {

  private static final String DEFAULT_LOGIN = "johndoe";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private EntityManager em;

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private MockMvc restUserMockMvc;

  private User user;
  private Long numberOfUsers;

  @BeforeEach
  public void countUsers() {
    numberOfUsers = userRepository.count();
  }

  @BeforeEach
  public void initTest() {
    user = UserResourceIT.initTestUser(em);
  }

  @AfterEach
  public void cleanupAndCheck() {
    cacheManager
        .getCacheNames()
        .stream()
        .map(cacheName -> this.cacheManager.getCache(cacheName))
        .filter(Objects::nonNull)
        .forEach(Cache::clear);
    userService.deleteUser(user.getLogin());
    assertThat(userRepository.count()).isEqualTo(numberOfUsers);
    numberOfUsers = null;
  }

  @Test
  @Transactional
  void getAllPublicUsers() throws Exception {
    // Initialize the database
    userRepository.saveAndFlush(user);

    // Get all the users
    restUserMockMvc
        .perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[?(@.id == %d)].login", user.getId()).value(user.getLogin()))
        .andExpect(jsonPath("$.[?(@.id == %d)].keys()", user.getId()).value(Set.of("id", "login")))
        .andExpect(jsonPath("$.[*].email").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].imageUrl").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.[*].langKey").doesNotHaveJsonPath());
  }

  @Test
  @Transactional
  void getAllUsersSortedByParameters() throws Exception {
    // Initialize the database
    userRepository.saveAndFlush(user);

    restUserMockMvc.perform(get("/api/users?sort=resetKey,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    restUserMockMvc.perform(get("/api/users?sort=password,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    restUserMockMvc
        .perform(get("/api/users?sort=resetKey,desc&sort=id,desc").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    restUserMockMvc.perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }
}
