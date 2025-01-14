/*
 * Copyright 2024 IQKV Team.
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
import com.iqkv.sample.webmvc.dashboard.domain.User;
import com.iqkv.sample.webmvc.dashboard.repository.UserRepository;
import com.iqkv.sample.webmvc.dashboard.web.rest.vm.LoginVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuthenticateController} REST controller.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AuthenticateControllerIT {

  @Autowired
  private ObjectMapper om;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @Transactional
  void testAuthorize() throws Exception {
    User user = new User();
    user.setLogin("user-jwt-controller");
    user.setEmail("user-jwt-controller@example.com");
    user.setActivated(true);
    user.setPassword(passwordEncoder.encode("test"));

    userRepository.saveAndFlush(user);

    LoginVM login = new LoginVM();
    login.setUsername("user-jwt-controller");
    login.setPassword("test");
    mockMvc
        .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id_token").isString())
        .andExpect(jsonPath("$.id_token").isNotEmpty())
        .andExpect(header().string("Authorization", not(nullValue())))
        .andExpect(header().string("Authorization", not(is(emptyString()))));
  }

  @Test
  @Transactional
  void testAuthorizeWithRememberMe() throws Exception {
    User user = new User();
    user.setLogin("user-jwt-controller-remember-me");
    user.setEmail("user-jwt-controller-remember-me@example.com");
    user.setActivated(true);
    user.setPassword(passwordEncoder.encode("test"));

    userRepository.saveAndFlush(user);

    LoginVM login = new LoginVM();
    login.setUsername("user-jwt-controller-remember-me");
    login.setPassword("test");
    login.setRememberMe(true);
    mockMvc
        .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id_token").isString())
        .andExpect(jsonPath("$.id_token").isNotEmpty())
        .andExpect(header().string("Authorization", not(nullValue())))
        .andExpect(header().string("Authorization", not(is(emptyString()))));
  }

  @Test
  void testAuthorizeFails() throws Exception {
    LoginVM login = new LoginVM();
    login.setUsername("wrong-user");
    login.setPassword("wrong password");
    mockMvc
        .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.id_token").doesNotExist())
        .andExpect(header().doesNotExist("Authorization"));
  }
}
