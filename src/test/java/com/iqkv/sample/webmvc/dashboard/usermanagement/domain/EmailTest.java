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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for Email value object.
 */
class EmailTest {

  @Test
  void shouldCreateValidEmail() {
    // Given
    String validEmail = "test@example.com";

    // When
    Email email = new Email(validEmail);

    // Then
    assertThat(email.getValue()).isEqualTo(validEmail.toLowerCase());
  }

  @Test
  void shouldNormalizeEmailToLowerCase() {
    // Given
    String mixedCaseEmail = "Test.User@EXAMPLE.COM";

    // When
    Email email = new Email(mixedCaseEmail);

    // Then
    assertThat(email.getValue()).isEqualTo("test.user@example.com");
  }

  @Test
  void shouldTrimWhitespace() {
    // Given
    String emailWithWhitespace = "  test@example.com  ";

    // When
    Email email = new Email(emailWithWhitespace);

    // Then
    assertThat(email.getValue()).isEqualTo("test@example.com");
  }

  @Test
  void shouldThrowExceptionForNullEmail() {
    // When & Then
    assertThatThrownBy(() -> new Email(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionForEmptyEmail() {
    // When & Then
    assertThatThrownBy(() -> new Email(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionForWhitespaceOnlyEmail() {
    // When & Then
    assertThatThrownBy(() -> new Email("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionForInvalidEmailFormat() {
    // When & Then
    assertThatThrownBy(() -> new Email("invalid-email"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email format: invalid-email");

    assertThatThrownBy(() -> new Email("@example.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email format: @example.com");

    assertThatThrownBy(() -> new Email("test@"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email format: test@");

    assertThatThrownBy(() -> new Email("test.example.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email format: test.example.com");
  }

  @Test
  void shouldAcceptValidEmailFormats() {
    // Valid email formats should not throw exceptions
    new Email("test@example.com");
    new Email("user.name@example.com");
    new Email("user+tag@example.com");
    new Email("user123@example-domain.com");
    new Email("test@subdomain.example.com");
    new Email("a@b.co");
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    Email email1 = new Email("test@example.com");
    Email email2 = new Email("TEST@EXAMPLE.COM"); // Should be normalized
    Email email3 = new Email("different@example.com");

    // When & Then
    assertThat(email1).isEqualTo(email2);
    assertThat(email1).isNotEqualTo(email3);
    assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    Email email = new Email("test@example.com");

    // When & Then
    assertThat(email.toString()).isEqualTo("test@example.com");
  }

  // Enhanced domain logic tests

  @Test
  void shouldExtractDomainPart() {
    // Given
    Email email = new Email("user@example.com");

    // When
    String domain = email.getDomain();

    // Then
    assertThat(domain).isEqualTo("example.com");
  }

  @Test
  void shouldExtractLocalPart() {
    // Given
    Email email = new Email("user@example.com");

    // When
    String localPart = email.getLocalPart();

    // Then
    assertThat(localPart).isEqualTo("user");
  }

  @Test
  void shouldDetectDisposableEmail() {
    // Given
    Email disposableEmail = new Email("test@10minutemail.com");
    Email regularEmail = new Email("test@example.com");

    // When & Then
    assertThat(disposableEmail.isDisposable()).isTrue();
    assertThat(regularEmail.isDisposable()).isFalse();
  }

  @Test
  void shouldDetectCorporateEmail() {
    // Given
    Email corporateEmail = new Email("test@gmail.com");
    Email businessEmail = new Email("test@company.com");

    // When & Then
    assertThat(corporateEmail.isCorporateEmail()).isTrue();
    assertThat(businessEmail.isCorporateEmail()).isFalse();
  }

  @Test
  void shouldDetectBusinessEmail() {
    // Given
    Email businessEmail = new Email("test@company.com");
    Email corporateEmail = new Email("test@gmail.com");

    // When & Then
    assertThat(businessEmail.isBusinessEmail()).isTrue();
    assertThat(corporateEmail.isBusinessEmail()).isFalse();
  }

  @Test
  void shouldCreateMaskedEmail() {
    // Given
    Email email = new Email("john.doe@example.com");

    // When
    String masked = email.getMaskedEmail();

    // Then
    assertThat(masked).isEqualTo("j******e@example.com");
  }

  @Test
  void shouldCreateMaskedEmailForShortLocalPart() {
    // Given
    Email email = new Email("jo@example.com");

    // When
    String masked = email.getMaskedEmail();

    // Then
    assertThat(masked).isEqualTo("j*@example.com");
  }

  @Test
  void shouldValidateBusinessSuitability() {
    // Given
    Email businessEmail = new Email("user@company.com");
    Email disposableEmail = new Email("user@10minutemail.com");

    // When & Then
    assertThat(businessEmail.isBusinessSuitable()).isTrue();
    assertThat(disposableEmail.isBusinessSuitable()).isFalse();
  }

  @Test
  void shouldCreateEmailUsingFactoryMethod() {
    // Given
    String emailString = "test@example.com";

    // When
    Email email = Email.of(emailString);

    // Then
    assertThat(email.getValue()).isEqualTo(emailString);
  }

  @Test
  void shouldValidateEmailString() {
    // When & Then
    assertThat(Email.isValid("test@example.com")).isTrue();
    assertThat(Email.isValid("invalid-email")).isFalse();
    assertThat(Email.isValid(null)).isFalse();
    assertThat(Email.isValid("")).isFalse();
  }

  @Test
  void shouldNormalizeEmailString() {
    // Given
    String emailString = "  Test@Example.COM  ";

    // When
    String normalized = Email.normalize(emailString);

    // Then
    assertThat(normalized).isEqualTo("test@example.com");
  }

  @Test
  void shouldHandleNullInNormalize() {
    // When
    String normalized = Email.normalize(null);

    // Then
    assertThat(normalized).isNull();
  }
}
