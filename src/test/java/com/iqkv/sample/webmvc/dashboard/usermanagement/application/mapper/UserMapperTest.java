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

package com.iqkv.sample.webmvc.dashboard.usermanagement.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.Authority;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserMapper within the User Management bounded context.
 * <p>
 * Tests the application layer's mapping between domain entities and DTOs,
 * ensuring proper data transformation and validation.
 */
class UserMapperTest {

  private static final String DEFAULT_LOGIN = "johndoe";
  private static final Long DEFAULT_ID = 1L;
  private static final String DEFAULT_EMAIL = "johndoe@localhost";
  private static final String DEFAULT_FIRSTNAME = "john";
  private static final String DEFAULT_LASTNAME = "doe";
  private static final String DEFAULT_IMAGEURL = "image_url";
  private static final String DEFAULT_LANGKEY = "en";

  private UserMapper userMapper;
  private User testUser;
  private AdminUserDTO testUserDto;

  @BeforeEach
  void setUp() {
    userMapper = new UserMapper();
    testUser = createTestUser();
    testUserDto = new AdminUserDTO(testUser);
  }

  @Test
  void shouldMapUserToAdminUserDTO() {
    // When
    AdminUserDTO convertedUserDto = userMapper.userToAdminUserDTO(testUser);

    // Then
    assertThat(convertedUserDto.getId()).isEqualTo(testUser.getId());
    assertThat(convertedUserDto.getLogin()).isEqualTo(testUser.getLogin());
    assertThat(convertedUserDto.getFirstName()).isEqualTo(testUser.getFirstName());
    assertThat(convertedUserDto.getLastName()).isEqualTo(testUser.getLastName());
    assertThat(convertedUserDto.getEmail()).isEqualTo(testUser.getEmail());
    assertThat(convertedUserDto.isActivated()).isEqualTo(testUser.isActivated());
    assertThat(convertedUserDto.getImageUrl()).isEqualTo(testUser.getImageUrl());
    assertThat(convertedUserDto.getCreatedBy()).isEqualTo(testUser.getCreatedBy());
    assertThat(convertedUserDto.getCreatedDate()).isEqualTo(testUser.getCreatedDate());
    assertThat(convertedUserDto.getLastModifiedBy()).isEqualTo(testUser.getLastModifiedBy());
    assertThat(convertedUserDto.getLastModifiedDate()).isEqualTo(testUser.getLastModifiedDate());
    assertThat(convertedUserDto.getLangKey()).isEqualTo(testUser.getLangKey());
    assertThat(convertedUserDto.getAuthorities()).containsExactly(AuthoritiesConstants.USER);
  }

  @Test
  void shouldMapAdminUserDTOToUser() {
    // When
    User convertedUser = userMapper.userDTOToUser(testUserDto);

    // Then
    assertThat(convertedUser.getId()).isEqualTo(testUserDto.getId());
    assertThat(convertedUser.getLogin()).isEqualTo(testUserDto.getLogin());
    assertThat(convertedUser.getFirstName()).isEqualTo(testUserDto.getFirstName());
    assertThat(convertedUser.getLastName()).isEqualTo(testUserDto.getLastName());
    assertThat(convertedUser.getEmail()).isEqualTo(testUserDto.getEmail());
    assertThat(convertedUser.isActivated()).isEqualTo(testUserDto.isActivated());
    assertThat(convertedUser.getImageUrl()).isEqualTo(testUserDto.getImageUrl());
    assertThat(convertedUser.getLangKey()).isEqualTo(testUserDto.getLangKey());
    assertThat(convertedUser.getCreatedBy()).isEqualTo(testUserDto.getCreatedBy());
    assertThat(convertedUser.getCreatedDate()).isEqualTo(testUserDto.getCreatedDate());
    assertThat(convertedUser.getLastModifiedBy()).isEqualTo(testUserDto.getLastModifiedBy());
    assertThat(convertedUser.getLastModifiedDate()).isEqualTo(testUserDto.getLastModifiedDate());
    assertThat(convertedUser.getAuthorities()).extracting("name").containsExactly(AuthoritiesConstants.USER);
  }

  @Test
  void shouldMapUsersToUserDTOsFilteringNulls() {
    // Given
    List<User> users = new ArrayList<>();
    users.add(testUser);
    users.add(null);

    // When
    List<UserDTO> userDTOs = userMapper.usersToUserDTOs(users);

    // Then
    assertThat(userDTOs).hasSize(1);
    assertThat(userDTOs.get(0).getLogin()).isEqualTo(testUser.getLogin());
  }

  @Test
  void shouldMapUserDTOsToUsersFilteringNulls() {
    // Given
    List<AdminUserDTO> userDtos = new ArrayList<>();
    userDtos.add(testUserDto);
    userDtos.add(null);

    // When
    List<User> users = userMapper.userDTOsToUsers(userDtos);

    // Then
    assertThat(users).hasSize(1);
    assertThat(users.get(0).getLogin()).isEqualTo(testUserDto.getLogin());
  }

  @Test
  void shouldMapAuthoritiesFromStringsToDomain() {
    // Given
    Set<String> authoritiesAsString = new HashSet<>();
    authoritiesAsString.add(AuthoritiesConstants.ADMIN);
    testUserDto.setAuthorities(authoritiesAsString);

    List<AdminUserDTO> userDtos = new ArrayList<>();
    userDtos.add(testUserDto);

    // When
    List<User> users = userMapper.userDTOsToUsers(userDtos);

    // Then
    assertThat(users).hasSize(1);
    User mappedUser = users.get(0);
    assertThat(mappedUser.getAuthorities()).isNotNull();
    assertThat(mappedUser.getAuthorities()).isNotEmpty();
    assertThat(mappedUser.getAuthorities().iterator().next().getName()).isEqualTo(AuthoritiesConstants.ADMIN);
  }

  @Test
  void shouldHandleNullAuthoritiesGracefully() {
    // Given
    testUserDto.setAuthorities(null);

    List<AdminUserDTO> userDtos = new ArrayList<>();
    userDtos.add(testUserDto);

    // When
    List<User> users = userMapper.userDTOsToUsers(userDtos);

    // Then
    assertThat(users).hasSize(1);
    User mappedUser = users.get(0);
    assertThat(mappedUser.getAuthorities()).isNotNull();
    assertThat(mappedUser.getAuthorities()).isEmpty();
  }

  @Test
  void shouldMapUserDTOToUserWithAuthorities() {
    // When
    User convertedUser = userMapper.userDTOToUser(testUserDto);

    // Then
    assertThat(convertedUser).isNotNull();
    assertThat(convertedUser.getAuthorities()).isNotNull();
    assertThat(convertedUser.getAuthorities()).isNotEmpty();
    assertThat(convertedUser.getAuthorities().iterator().next().getName()).isEqualTo(AuthoritiesConstants.USER);
  }

  @Test
  void shouldMapUserDTOToUserWithEmptyAuthoritiesWhenNull() {
    // Given
    testUserDto.setAuthorities(null);

    // When
    User mappedUser = userMapper.userDTOToUser(testUserDto);

    // Then
    assertThat(mappedUser).isNotNull();
    assertThat(mappedUser.getAuthorities()).isNotNull();
    assertThat(mappedUser.getAuthorities()).isEmpty();
  }

  @Test
  void shouldReturnNullWhenMappingNullUserDTO() {
    // When & Then
    assertThat(userMapper.userDTOToUser(null)).isNull();
  }

  @Test
  void shouldReturnNullWhenMappingNullUser() {
    // When & Then
    assertThat(userMapper.userToAdminUserDTO(null)).isNull();
  }

  @Test
  void shouldCreateUserFromId() {
    // When
    User userFromId = userMapper.userFromId(DEFAULT_ID);

    // Then
    assertThat(userFromId.getId()).isEqualTo(DEFAULT_ID);
  }

  @Test
  void shouldReturnNullWhenCreatingUserFromNullId() {
    // When & Then
    assertThat(userMapper.userFromId(null)).isNull();
  }

  @Test
  void shouldMapComplexUserWithMultipleAuthorities() {
    // Given
    Set<Authority> authorities = new HashSet<>();
    authorities.add(createAuthority(AuthoritiesConstants.USER));
    authorities.add(createAuthority(AuthoritiesConstants.ADMIN));
    testUser.setAuthorities(authorities);

    // When
    AdminUserDTO convertedDto = userMapper.userToAdminUserDTO(testUser);

    // Then
    assertThat(convertedDto.getAuthorities()).hasSize(2);
    assertThat(convertedDto.getAuthorities()).containsExactlyInAnyOrder(
        AuthoritiesConstants.USER,
        AuthoritiesConstants.ADMIN
    );
  }

  @Test
  void shouldPreserveAuditingInformation() {
    // Given
    Instant createdDate = Instant.now().minusSeconds(3600);
    Instant modifiedDate = Instant.now();
    testUser.setCreatedBy("system");
    testUser.setCreatedDate(createdDate);
    testUser.setLastModifiedBy("admin");
    testUser.setLastModifiedDate(modifiedDate);

    // When
    AdminUserDTO convertedDto = userMapper.userToAdminUserDTO(testUser);

    // Then
    assertThat(convertedDto.getCreatedBy()).isEqualTo("system");
    assertThat(convertedDto.getCreatedDate()).isEqualTo(createdDate);
    assertThat(convertedDto.getLastModifiedBy()).isEqualTo("admin");
    assertThat(convertedDto.getLastModifiedDate()).isEqualTo(modifiedDate);
  }

  @Test
  void shouldHandleEmptyCollections() {
    // Given
    List<User> emptyUsers = new ArrayList<>();
    List<AdminUserDTO> emptyDtos = new ArrayList<>();

    // When
    List<UserDTO> mappedDtos = userMapper.usersToUserDTOs(emptyUsers);
    List<User> mappedUsers = userMapper.userDTOsToUsers(emptyDtos);

    // Then
    assertThat(mappedDtos).isEmpty();
    assertThat(mappedUsers).isEmpty();
  }

  private User createTestUser() {
    User user = new User();
    user.setId(DEFAULT_ID);
    user.setLogin(DEFAULT_LOGIN);
    user.setPassword(RandomStringUtils.randomAlphanumeric(60));
    user.setActivated(true);
    user.setEmail(DEFAULT_EMAIL);
    user.setFirstName(DEFAULT_FIRSTNAME);
    user.setLastName(DEFAULT_LASTNAME);
    user.setImageUrl(DEFAULT_IMAGEURL);
    user.setCreatedBy(DEFAULT_LOGIN);
    user.setCreatedDate(Instant.now());
    user.setLastModifiedBy(DEFAULT_LOGIN);
    user.setLastModifiedDate(Instant.now());
    user.setLangKey(DEFAULT_LANGKEY);

    Set<Authority> authorities = new HashSet<>();
    authorities.add(createAuthority(AuthoritiesConstants.USER));
    user.setAuthorities(authorities);

    return user;
  }

  private Authority createAuthority(String name) {
    Authority authority = new Authority();
    authority.setName(name);
    return authority;
  }
}
