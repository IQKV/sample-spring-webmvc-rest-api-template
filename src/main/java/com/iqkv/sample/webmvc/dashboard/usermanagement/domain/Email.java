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

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Email value object that ensures email format validation and business rules.
 * <p>
 * This value object encapsulates email-related business logic including
 * validation, normalization, and domain-specific operations.
 */
@Embeddable
public class Email {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

  private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
      "10minutemail.com", "tempmail.org", "guerrillamail.com",
      "mailinator.com", "throwaway.email"
  );

  private static final Set<String> CORPORATE_EMAIL_DOMAINS = Set.of(
      "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
      "icloud.com", "protonmail.com"
  );

  private final String value;

  protected Email() {
    this.value = null; // For JPA
  }

  public Email(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

    String normalizedEmail = email.trim().toLowerCase();
    if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
      throw new IllegalArgumentException("Invalid email format: " + email);
    }

    this.value = normalizedEmail;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Email email = (Email) o;
    return Objects.equals(value, email.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }

  // Domain Logic Methods

  /**
   * Gets the domain part of the email address.
   *
   * @return the domain part (e.g., "example.com" from "user@example.com")
   */
  public String getDomain() {
    if (value == null) {
      return null;
    }
    int atIndex = value.indexOf('@');
    return atIndex > 0 ? value.substring(atIndex + 1) : null;
  }

  /**
   * Gets the local part of the email address.
   *
   * @return the local part (e.g., "user" from "user@example.com")
   */
  public String getLocalPart() {
    if (value == null) {
      return null;
    }
    int atIndex = value.indexOf('@');
    return atIndex > 0 ? value.substring(0, atIndex) : null;
  }

  /**
   * Checks if this email uses a disposable email service.
   *
   * @return true if the email domain is known to be disposable
   */
  public boolean isDisposable() {
    String domain = getDomain();
    return domain != null && DISPOSABLE_EMAIL_DOMAINS.contains(domain.toLowerCase());
  }

  /**
   * Checks if this email uses a corporate/personal email service.
   *
   * @return true if the email domain is a well-known personal email provider
   */
  public boolean isCorporateEmail() {
    String domain = getDomain();
    return domain != null && CORPORATE_EMAIL_DOMAINS.contains(domain.toLowerCase());
  }

  /**
   * Checks if this email appears to be from a business domain.
   *
   * @return true if the email domain is likely a business domain
   */
  public boolean isBusinessEmail() {
    return !isCorporateEmail() && !isDisposable();
  }

  /**
   * Creates a masked version of the email for display purposes.
   *
   * @return masked email (e.g., "u***@example.com")
   */
  public String getMaskedEmail() {
    if (value == null) {
      return null;
    }

    String localPart = getLocalPart();
    String domain = getDomain();

    if (localPart == null || domain == null) {
      return value;
    }

    if (localPart.length() <= 2) {
      return localPart.charAt(0) + "*@" + domain;
    }

    return localPart.charAt(0) + "*".repeat(localPart.length() - 2) +
           localPart.charAt(localPart.length() - 1) + "@" + domain;
  }

  /**
   * Validates if the email is suitable for business use.
   *
   * @return true if the email is acceptable for business purposes
   */
  public boolean isBusinessSuitable() {
    return !isDisposable() && value != null && value.length() >= 5;
  }

  /**
   * Creates an Email instance from a string value.
   *
   * @param emailString the email string
   * @return a new Email instance
   * @throws IllegalArgumentException if the email format is invalid
   */
  public static Email of(String emailString) {
    return new Email(emailString);
  }

  /**
   * Validates an email string without creating an instance.
   *
   * @param emailString the email string to validate
   * @return true if the email format is valid
   */
  public static boolean isValid(String emailString) {
    if (emailString == null || emailString.trim().isEmpty()) {
      return false;
    }

    String normalizedEmail = emailString.trim().toLowerCase();
    return EMAIL_PATTERN.matcher(normalizedEmail).matches();
  }

  /**
   * Normalizes an email string (lowercase, trimmed).
   *
   * @param emailString the email string to normalize
   * @return normalized email string
   */
  public static String normalize(String emailString) {
    if (emailString == null) {
      return null;
    }
    return emailString.trim().toLowerCase();
  }
}
