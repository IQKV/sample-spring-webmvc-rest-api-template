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

package com.iqkv.sample.webmvc.dashboard.usermanagement.domain;

import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertAuthorityDisplayName;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertAuthorityHasHigherLevel;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertAuthorityIsValid;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertAuthorityLevel;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertAuthorityName;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertIsNotSystemAuthority;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityAsserts.assertIsSystemAuthority;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityTestSamples.getAuthoritySample1;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityTestSamples.getAuthoritySample2;
import static com.iqkv.sample.webmvc.dashboard.usermanagement.domain.AuthorityTestSamples.getCustomAuthoritySample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Authority domain entity within the User Management bounded context.
 * <p>
 * Tests the domain logic, validation rules, and business constraints
 * for the Authority entity.
 */
class AuthorityTest {

  private Authority authority;

  @BeforeEach
  void setUp() {
    authority = new Authority();
  }

  @Test
  void shouldCreateAuthorityWithValidName() {
    // Given
    String authorityName = AuthoritiesConstants.USER;

    // When
    authority.setName(authorityName);

    // Then
    assertThat(authority.getName()).isEqualTo(authorityName);
  }

  @Test
  void shouldValidateAuthorityName() {
    // Given
    String validName = AuthoritiesConstants.ADMIN;

    // When
    authority.setName(validName);

    // Then
    assertAuthorityName(authority, validName);
    assertAuthorityIsValid(authority);
  }

  @Test
  void shouldRejectNullAuthorityName() {
    // When & Then
    assertThatThrownBy(() -> authority.setName(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authority name cannot be null or empty");
  }

  @Test
  void shouldRejectEmptyAuthorityName() {
    // When & Then
    assertThatThrownBy(() -> authority.setName(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authority name cannot be null or empty");
  }

  @Test
  void shouldRejectBlankAuthorityName() {
    // When & Then
    assertThatThrownBy(() -> authority.setName("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authority name cannot be null or empty");
  }

  @Test
  void shouldValidateAuthorityNameFormat() {
    // Given
    String invalidName = "invalid-authority";

    // When & Then
    assertThatThrownBy(() -> authority.setName(invalidName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authority name must start with ROLE_");
  }

  @Test
  void shouldAcceptValidRoleNames() {
    // Given & When & Then
    authority.setName("ROLE_USER");
    assertThat(authority.getName()).isEqualTo("ROLE_USER");

    authority.setName("ROLE_ADMIN");
    assertThat(authority.getName()).isEqualTo("ROLE_ADMIN");

    authority.setName("ROLE_MANAGER");
    assertThat(authority.getName()).isEqualTo("ROLE_MANAGER");
  }

  @Test
  void shouldCheckIfSystemAuthority() {
    // Given
    Authority userAuthority = getAuthoritySample1();
    Authority adminAuthority = getAuthoritySample2();
    Authority customAuthority = getCustomAuthoritySample();

    // When & Then
    assertIsSystemAuthority(userAuthority);
    assertIsSystemAuthority(adminAuthority);
    assertIsNotSystemAuthority(customAuthority);
  }

  @Test
  void shouldGetAuthorityLevel() {
    // Given
    Authority userAuthority = getAuthoritySample1();
    Authority adminAuthority = getAuthoritySample2();

    // When & Then
    assertAuthorityLevel(userAuthority, 1);
    assertAuthorityLevel(adminAuthority, 10);
  }

  @Test
  void shouldCompareAuthorities() {
    // Given
    Authority userAuthority = getAuthoritySample1();
    Authority adminAuthority = getAuthoritySample2();

    // When & Then
    assertAuthorityHasHigherLevel(adminAuthority, userAuthority);
    assertThat(userAuthority.hasHigherLevelThan(adminAuthority)).isFalse();
    assertThat(userAuthority.hasHigherLevelThan(userAuthority)).isFalse();
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    Authority authority1 = getAuthoritySample1();
    Authority authority2 = getAuthoritySample1();
    Authority authority3 = getAuthoritySample2();

    // When & Then
    TestUtil.equalsVerifier(Authority.class);
    assertThat(authority1).isEqualTo(authority2);
    assertThat(authority1).isNotEqualTo(authority3);
    assertThat(authority1.hashCode()).isEqualTo(authority2.hashCode());
    assertThat(authority1.hashCode()).isNotEqualTo(authority3.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    authority.setName(AuthoritiesConstants.USER);

    // When
    String result = authority.toString();

    // Then
    assertThat(result).contains("Authority{");
    assertThat(result).contains("name='ROLE_USER'");
  }

  @Test
  void shouldCreateFromString() {
    // Given
    String authorityName = AuthoritiesConstants.ADMIN;

    // When
    Authority authority = Authority.of(authorityName);

    // Then
    assertThat(authority.getName()).isEqualTo(authorityName);
  }

  @Test
  void shouldGetDisplayName() {
    // Given
    authority.setName("ROLE_SUPER_ADMIN");

    // When & Then
    assertAuthorityDisplayName(authority, "Super Admin");
  }
}
