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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for UserManagementController.
 */
@WebMvcTest(UserManagementController.class)
class UserManagementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserManagementService userManagementService;

  private AdminUserDTO adminUserDTO;
  private User user;

  @BeforeEach
  void setUp() {
    adminUserDTO = new AdminUserDTO();
    adminUserDTO.setLogin("testuser");
    adminUserDTO.setEmail("test@example.com");
    adminUserDTO.setFirstName("Test");
    adminUserDTO.setLastName("User");
    adminUserDTO.setLangKey("en");
    adminUserDTO.setActivated(true);
    adminUserDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));

    user = new User();
    user.setId(1L);
    user.setLogin("testuser");
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setActivated(true);
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldCreateUser() throws Exception {
    // Given
    when(userManagementService.createUser(any(AdminUserDTO.class))).thenReturn(user);

    // When & Then
    mockMvc.perform(post("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminUserDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.login").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldReturnBadRequestWhenCreatingUserWithId() throws Exception {
    // Given
    adminUserDTO.setId(1L); // ID should not be set for creation

    // When & Then
    mockMvc.perform(post("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminUserDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldUpdateUser() throws Exception {
    // Given
    adminUserDTO.setId(1L);
    when(userManagementService.updateUser(any(AdminUserDTO.class))).thenReturn(Optional.of(adminUserDTO));

    // When & Then
    mockMvc.perform(put("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminUserDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.login").value("testuser"));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
    // Given
    adminUserDTO.setId(999L);
    when(userManagementService.updateUser(any(AdminUserDTO.class))).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(put("/api/admin/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminUserDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldGetAllUsers() throws Exception {
    // Given
    Page<AdminUserDTO> page = new PageImpl<>(List.of(adminUserDTO), PageRequest.of(0, 20), 1);
    when(userManagementService.getAllManagedUsers(any())).thenReturn(page);

    // When & Then
    mockMvc.perform(get("/api/admin/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].login").value("testuser"));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldGetUser() throws Exception {
    // Given
    when(userManagementService.getUserWithAuthoritiesByLogin("testuser")).thenReturn(Optional.of(user));

    // When & Then
    mockMvc.perform(get("/api/admin/users/testuser"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.login").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldReturnNotFoundWhenGettingNonExistentUser() throws Exception {
    // Given
    when(userManagementService.getUserWithAuthoritiesByLogin("nonexistent")).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/admin/users/nonexistent"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldDeleteUser() throws Exception {
    // When & Then
    mockMvc.perform(delete("/api/admin/users/testuser"))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  void shouldGetAuthorities() throws Exception {
    // Given
    when(userManagementService.getAuthorities()).thenReturn(List.of(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER));

    // When & Then
    mockMvc.perform(get("/api/admin/users/authorities"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").value(AuthoritiesConstants.ADMIN))
        .andExpect(jsonPath("$[1]").value(AuthoritiesConstants.USER));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.USER)
  void shouldReturnForbiddenForNonAdminUser() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/admin/users"))
        .andExpect(status().isForbidden());
  }
}