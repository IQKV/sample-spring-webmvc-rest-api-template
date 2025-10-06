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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for User domain entity.
 */
class UserTest {

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setLogin("testuser");
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setActivated(false);
  }

  @Test
  void shouldActivateUser() {
    // Given
    user.setActivationKey("activation-key");

    // When
    user.activate();

    // Then
    assertThat(user.isActivated()).isTrue();
    assertThat(user.getActivationKey()).isNull();
  }

  @Test
  void shouldThrowExceptionWhenActivatingAlreadyActivatedUser() {
    // Given
    user.setActivated(true);

    // When & Then
    assertThatThrownBy(() -> user.activate())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("User is already activated");
  }

  @Test
  void shouldDeactivateUser() {
    // Given
    user.setActivated(true);

    // When
    user.deactivate();

    // Then
    assertThat(user.isActivated()).isFalse();
  }

  @Test
  void shouldThrowExceptionWhenDeactivatingAlreadyDeactivatedUser() {
    // Given
    user.setActivated(false);

    // When & Then
    assertThatThrownBy(() -> user.deactivate())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("User is already deactivated");
  }

  @Test
  void shouldInitiatePasswordReset() {
    // Given
    String resetKey = "reset-key-123";

    // When
    user.initiatePasswordReset(resetKey);

    // Then
    assertThat(user.getResetKey()).isEqualTo(resetKey);
    assertThat(user.getResetDate()).isNotNull();
    assertThat(user.getResetDate()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void shouldThrowExceptionWhenInitiatingPasswordResetWithNullKey() {
    // When & Then
    assertThatThrownBy(() -> user.initiatePasswordReset(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Reset key cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenInitiatingPasswordResetWithEmptyKey() {
    // When & Then
    assertThatThrownBy(() -> user.initiatePasswordReset(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Reset key cannot be null or empty");
  }

  @Test
  void shouldCompletePasswordReset() {
    // Given
    String resetKey = "reset-key-123";
    String newPassword = "new-password";
    user.initiatePasswordReset(resetKey);

    // When
    user.completePasswordReset(newPassword);

    // Then
    assertThat(user.getPassword()).isEqualTo(newPassword);
    assertThat(user.getResetKey()).isNull();
    assertThat(user.getResetDate()).isNull();
  }

  @Test
  void shouldThrowExceptionWhenCompletingPasswordResetWithNullPassword() {
    // Given
    user.initiatePasswordReset("reset-key");

    // When & Then
    assertThatThrownBy(() -> user.completePasswordReset(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenCompletingPasswordResetWithExpiredKey() {
    // Given
    user.setResetKey("reset-key");
    user.setResetDate(Instant.now().minus(25, ChronoUnit.HOURS)); // Expired

    // When & Then
    assertThatThrownBy(() -> user.completePasswordReset("new-password"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Password reset key is invalid or expired");
  }

  @Test
  void shouldUpdateProfile() {
    // Given
    String firstName = "Updated";
    String lastName = "Name";
    String email = "updated@example.com";
    String langKey = "en";
    String imageUrl = "http://example.com/image.jpg";

    // When
    user.updateProfile(firstName, lastName, email, langKey, imageUrl);

    // Then
    assertThat(user.getFirstName()).isEqualTo(firstName);
    assertThat(user.getLastName()).isEqualTo(lastName);
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getLangKey()).isEqualTo(langKey);
    assertThat(user.getImageUrl()).isEqualTo(imageUrl);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingProfileWithInvalidEmail() {
    // When & Then
    assertThatThrownBy(() -> user.updateProfile("First", "Last", "invalid-email", "en", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email format");
  }

  @Test
  void shouldChangePassword() {
    // Given
    String newPassword = "new-secure-password";

    // When
    user.changePassword(newPassword);

    // Then
    assertThat(user.getPassword()).isEqualTo(newPassword);
  }

  @Test
  void shouldThrowExceptionWhenChangingPasswordWithNullPassword() {
    // When & Then
    assertThatThrownBy(() -> user.changePassword(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password cannot be null or empty");
  }

  @Test
  void shouldAddAuthority() {
    // Given
    Authority authority = new Authority();
    authority.setName("ROLE_USER");

    // When
    user.addAuthority(authority);

    // Then
    assertThat(user.getAuthorities()).contains(authority);
  }

  @Test
  void shouldThrowExceptionWhenAddingNullAuthority() {
    // When & Then
    assertThatThrownBy(() -> user.addAuthority(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authority cannot be null");
  }

  @Test
  void shouldRemoveAuthority() {
    // Given
    Authority authority = new Authority();
    authority.setName("ROLE_USER");
    user.addAuthority(authority);

    // When
    user.removeAuthority(authority);

    // Then
    assertThat(user.getAuthorities()).doesNotContain(authority);
  }

  @Test
  void shouldCheckIfUserHasAuthority() {
    // Given
    Authority authority = new Authority();
    authority.setName("ROLE_ADMIN");
    user.addAuthority(authority);

    // When & Then
    assertThat(user.hasAuthority("ROLE_ADMIN")).isTrue();
    assertThat(user.hasAuthority("ROLE_USER")).isFalse();
  }

  @Test
  void shouldValidatePasswordResetKey() {
    // Given - Valid reset key (within 24 hours)
    user.setResetKey("valid-key");
    user.setResetDate(Instant.now().minus(1, ChronoUnit.HOURS));

    // When & Then
    assertThat(user.isPasswordResetValid()).isTrue();

    // Given - Expired reset key (older than 24 hours)
    user.setResetDate(Instant.now().minus(25, ChronoUnit.HOURS));

    // When & Then
    assertThat(user.isPasswordResetValid()).isFalse();

    // Given - No reset key
    user.setResetKey(null);
    user.setResetDate(null);

    // When & Then
    assertThat(user.isPasswordResetValid()).isFalse();
  }

  @Test
  void shouldGetFullName() {
    // Given
    user.setFirstName("John");
    user.setLastName("Doe");

    // When & Then
    assertThat(user.getFullName()).isEqualTo("John Doe");

    // Given - Only first name
    user.setFirstName("John");
    user.setLastName(null);

    // When & Then
    assertThat(user.getFullName()).isEqualTo("John");

    // Given - Only last name
    user.setFirstName(null);
    user.setLastName("Doe");

    // When & Then
    assertThat(user.getFullName()).isEqualTo("Doe");

    // Given - No names
    user.setFirstName(null);
    user.setLastName(null);

    // When & Then
    assertThat(user.getFullName()).isEqualTo("");
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    User user1 = new User();
    user1.setId(1L);

    User user2 = new User();
    user2.setId(1L);

    User user3 = new User();
    user3.setId(2L);

    // When & Then
    assertThat(user1).isEqualTo(user2);
    assertThat(user1).isNotEqualTo(user3);
    assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
  }
}
