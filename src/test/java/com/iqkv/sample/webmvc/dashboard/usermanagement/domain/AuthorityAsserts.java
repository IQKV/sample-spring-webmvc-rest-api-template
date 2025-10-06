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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom assertions for Authority domain entity.
 * <p>
 * Provides domain-specific assertion methods for testing Authority entities
 * within the User Management bounded context.
 */
public class AuthorityAsserts {

  /**
   * Asserts that the Authority has the expected name.
   *
   * @param actual   the Authority to check
   * @param expected the expected name
   */
  public static void assertAuthorityName(Authority actual, String expected) {
    assertThat(actual).isNotNull();
    assertThat(actual.getName()).isEqualTo(expected);
  }

  /**
   * Asserts that the Authority is valid according to business rules.
   *
   * @param actual the Authority to check
   */
  public static void assertAuthorityIsValid(Authority actual) {
    assertThat(actual).isNotNull();
    assertThat(actual.isValid()).isTrue();
    assertThat(actual.getName()).isNotNull();
    assertThat(actual.getName()).startsWith("ROLE_");
  }

  /**
   * Asserts that the Authority is a system authority.
   *
   * @param actual the Authority to check
   */
  public static void assertIsSystemAuthority(Authority actual) {
    assertThat(actual).isNotNull();
    assertThat(actual.isSystemAuthority()).isTrue();
  }

  /**
   * Asserts that the Authority is not a system authority.
   *
   * @param actual the Authority to check
   */
  public static void assertIsNotSystemAuthority(Authority actual) {
    assertThat(actual).isNotNull();
    assertThat(actual.isSystemAuthority()).isFalse();
  }

  /**
   * Asserts that one Authority has higher level than another.
   *
   * @param higher the Authority that should have higher level
   * @param lower  the Authority that should have lower level
   */
  public static void assertAuthorityHasHigherLevel(Authority higher, Authority lower) {
    assertThat(higher).isNotNull();
    assertThat(lower).isNotNull();
    assertThat(higher.hasHigherLevelThan(lower)).isTrue();
    assertThat(higher.getLevel()).isGreaterThan(lower.getLevel());
  }

  /**
   * Asserts that the Authority has the expected display name.
   *
   * @param actual              the Authority to check
   * @param expectedDisplayName the expected display name
   */
  public static void assertAuthorityDisplayName(Authority actual, String expectedDisplayName) {
    assertThat(actual).isNotNull();
    assertThat(actual.getDisplayName()).isEqualTo(expectedDisplayName);
  }

  /**
   * Asserts that the Authority has the expected level.
   *
   * @param actual        the Authority to check
   * @param expectedLevel the expected level
   */
  public static void assertAuthorityLevel(Authority actual, int expectedLevel) {
    assertThat(actual).isNotNull();
    assertThat(actual.getLevel()).isEqualTo(expectedLevel);
  }
}
