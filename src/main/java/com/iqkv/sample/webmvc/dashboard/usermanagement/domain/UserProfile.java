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
 * User profile value object containing user's personal information.
 * <p>
 * This value object encapsulates user profile data with validation
 * and business logic for name formatting and localization.
 */
@Embeddable
public class UserProfile {

  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{1,50}$");
  private static final Pattern URL_PATTERN = Pattern.compile("^https?://.*\\.(jpg|jpeg|png|gif|webp)$", Pattern.CASE_INSENSITIVE);

  private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
      "en", "es", "fr", "de", "it", "pt", "ru", "zh", "ja", "ko"
  );

  private final String firstName;
  private final String lastName;
  private final String langKey;
  private final String imageUrl;

  protected UserProfile() {
    this.firstName = null;
    this.lastName = null;
    this.langKey = null;
    this.imageUrl = null;
  }

  public UserProfile(String firstName, String lastName, String langKey, String imageUrl) {
    this.firstName = validateAndNormalizeName(firstName, "First name");
    this.lastName = validateAndNormalizeName(lastName, "Last name");
    this.langKey = validateLanguageKey(langKey);
    this.imageUrl = validateImageUrl(imageUrl);
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getLangKey() {
    return langKey;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getFullName() {
    if (firstName == null && lastName == null) {
      return "";
    }
    if (firstName == null) {
      return lastName;
    }
    if (lastName == null) {
      return firstName;
    }
    return firstName + " " + lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserProfile that = (UserProfile) o;
    return Objects.equals(firstName, that.firstName) &&
           Objects.equals(lastName, that.lastName) &&
           Objects.equals(langKey, that.langKey) &&
           Objects.equals(imageUrl, that.imageUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, langKey, imageUrl);
  }

  @Override
  public String toString() {
    return "UserProfile{" +
           "firstName='" + firstName + '\'' +
           ", lastName='" + lastName + '\'' +
           ", langKey='" + langKey + '\'' +
           ", imageUrl='" + imageUrl + '\'' +
           '}';
  }

  // Domain Logic Methods

  /**
   * Gets the initials of the user (first letter of first and last name).
   *
   * @return user initials (e.g., "JD" for "John Doe")
   */
  public String getInitials() {
    StringBuilder initials = new StringBuilder();

    if (firstName != null && !firstName.isEmpty()) {
      initials.append(Character.toUpperCase(firstName.charAt(0)));
    }

    if (lastName != null && !lastName.isEmpty()) {
      initials.append(Character.toUpperCase(lastName.charAt(0)));
    }

    return initials.toString();
  }

  /**
   * Gets the display name in the format "Last, First".
   *
   * @return formatted display name
   */
  public String getDisplayName() {
    if (firstName == null && lastName == null) {
      return "";
    }
    if (firstName == null) {
      return lastName;
    }
    if (lastName == null) {
      return firstName;
    }
    return lastName + ", " + firstName;
  }

  /**
   * Checks if the profile has a complete name (both first and last name).
   *
   * @return true if both first and last names are present
   */
  public boolean hasCompleteName() {
    return firstName != null && !firstName.trim().isEmpty() &&
           lastName != null && !lastName.trim().isEmpty();
  }

  /**
   * Checks if the profile has a valid image URL.
   *
   * @return true if image URL is present and valid
   */
  public boolean hasValidImage() {
    return imageUrl != null && URL_PATTERN.matcher(imageUrl).matches();
  }

  /**
   * Checks if the language key is supported.
   *
   * @return true if the language is supported
   */
  public boolean hasSupportedLanguage() {
    return langKey != null && SUPPORTED_LANGUAGES.contains(langKey);
  }

  /**
   * Creates a new UserProfile with updated first name.
   *
   * @param newFirstName the new first name
   * @return a new UserProfile instance
   */
  public UserProfile withFirstName(String newFirstName) {
    return new UserProfile(newFirstName, lastName, langKey, imageUrl);
  }

  /**
   * Creates a new UserProfile with updated last name.
   *
   * @param newLastName the new last name
   * @return a new UserProfile instance
   */
  public UserProfile withLastName(String newLastName) {
    return new UserProfile(firstName, newLastName, langKey, imageUrl);
  }

  /**
   * Creates a new UserProfile with updated language key.
   *
   * @param newLangKey the new language key
   * @return a new UserProfile instance
   */
  public UserProfile withLanguage(String newLangKey) {
    return new UserProfile(firstName, lastName, newLangKey, imageUrl);
  }

  /**
   * Creates a new UserProfile with updated image URL.
   *
   * @param newImageUrl the new image URL
   * @return a new UserProfile instance
   */
  public UserProfile withImageUrl(String newImageUrl) {
    return new UserProfile(firstName, lastName, langKey, newImageUrl);
  }

  /**
   * Creates a UserProfile instance from individual components.
   *
   * @param firstName the first name
   * @param lastName  the last name
   * @param langKey   the language key
   * @param imageUrl  the image URL
   * @return a new UserProfile instance
   */
  public static UserProfile of(String firstName, String lastName, String langKey, String imageUrl) {
    return new UserProfile(firstName, lastName, langKey, imageUrl);
  }

  /**
   * Creates a minimal UserProfile with just names.
   *
   * @param firstName the first name
   * @param lastName  the last name
   * @return a new UserProfile instance
   */
  public static UserProfile withNames(String firstName, String lastName) {
    return new UserProfile(firstName, lastName, "en", null);
  }

  // Private validation methods

  private String validateAndNormalizeName(String name, String fieldName) {
    if (name == null || name.trim().isEmpty()) {
      return null;
    }

    String trimmedName = name.trim();
    if (!NAME_PATTERN.matcher(trimmedName).matches()) {
      throw new IllegalArgumentException(fieldName + " contains invalid characters or exceeds length limit");
    }

    return capitalizeFirstLetter(trimmedName);
  }

  private String validateLanguageKey(String langKey) {
    if (langKey == null) {
      return "en"; // Default to English
    }

    if (!SUPPORTED_LANGUAGES.contains(langKey)) {
      throw new IllegalArgumentException("Unsupported language key: " + langKey);
    }

    return langKey;
  }

  private String validateImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.trim().isEmpty()) {
      return null;
    }

    String trimmedUrl = imageUrl.trim();
    if (!URL_PATTERN.matcher(trimmedUrl).matches()) {
      throw new IllegalArgumentException("Invalid image URL format");
    }

    return trimmedUrl;
  }

  private String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
  }
}
