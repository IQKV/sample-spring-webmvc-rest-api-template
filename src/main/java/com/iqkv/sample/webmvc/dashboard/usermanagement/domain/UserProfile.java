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

/**
 * User profile value object containing user's personal information.
 */
@Embeddable
public class UserProfile {

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
    this.firstName = firstName != null ? firstName.trim() : null;
    this.lastName = lastName != null ? lastName.trim() : null;
    this.langKey = langKey;
    this.imageUrl = imageUrl;
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
    return Objects.equals(firstName, that.firstName)
           && Objects.equals(lastName, that.lastName)
           && Objects.equals(langKey, that.langKey)
           && Objects.equals(imageUrl, that.imageUrl);
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
}
