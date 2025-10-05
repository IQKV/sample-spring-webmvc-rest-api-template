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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for Authority entity.
 */
class AuthorityTest {

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    Authority authority1 = new Authority();
    authority1.setName("ROLE_USER");

    Authority authority2 = new Authority();
    authority2.setName("ROLE_USER");

    Authority authority3 = new Authority();
    authority3.setName("ROLE_ADMIN");

    // When & Then
    assertThat(authority1).isEqualTo(authority2);
    assertThat(authority1).isNotEqualTo(authority3);
    assertThat(authority1.hashCode()).isEqualTo(authority2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    Authority authority = new Authority();
    authority.setName("ROLE_USER");

    // When
    String result = authority.toString();

    // Then
    assertThat(result).contains("Authority{");
    assertThat(result).contains("name=ROLE_USER");
  }

  @Test
  void shouldSetAndGetName() {
    // Given
    Authority authority = new Authority();
    String name = "ROLE_ADMIN";

    // When
    authority.setName(name);

    // Then
    assertThat(authority.getName()).isEqualTo(name);
    assertThat(authority.getId()).isEqualTo(name); // ID is the same as name
  }

  @Test
  void shouldTestPersistableInterface() {
    // Given
    Authority authority = new Authority();

    // When & Then - New entity
    assertThat(authority.isNew()).isTrue();

    // When - Set name and simulate persistence
    authority.setName("ROLE_USER");
    authority.setIsPersisted();

    // Then - Persisted entity
    assertThat(authority.isNew()).isFalse();
  }

  @Test
  void shouldTestFluentInterface() {
    // Given
    Authority authority = new Authority();

    // When
    Authority result = authority.name("ROLE_USER");

    // Then
    assertThat(result).isSameAs(authority);
    assertThat(authority.getName()).isEqualTo("ROLE_USER");
  }
}
