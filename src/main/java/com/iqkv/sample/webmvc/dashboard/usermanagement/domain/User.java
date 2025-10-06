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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iqkv.sample.webmvc.dashboard.config.AppConstants;
import com.iqkv.sample.webmvc.dashboard.shared.domain.AbstractAuditingEntity;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * User aggregate root representing a user in the system.
 * This is the main entity in the User Management bounded context.
 */
@Entity
@Table(name = "knowhow_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends AbstractAuditingEntity<Long> implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
  @SequenceGenerator(name = "sequenceGenerator")
  private Long id;

  @NotNull
  @Pattern(regexp = AppConstants.LOGIN_REGEX)
  @Size(min = 1, max = 50)
  @Column(length = 50, unique = true, nullable = false)
  private String login;

  @JsonIgnore
  @NotNull
  @Size(min = 60, max = 60)
  @Column(name = "password_hash", length = 60, nullable = false)
  private String password;

  @Size(max = 50)
  @Column(name = "first_name", length = 50)
  private String firstName;

  @Size(max = 50)
  @Column(name = "last_name", length = 50)
  private String lastName;

  @Email
  @Size(min = 5, max = 254)
  @Column(length = 254, unique = true)
  private String email;

  @NotNull
  @Column(nullable = false)
  private boolean activated = false;

  @Size(min = 2, max = 10)
  @Column(name = "lang_key", length = 10)
  private String langKey;

  @Size(max = 256)
  @Column(name = "image_url", length = 256)
  private String imageUrl;

  @Size(max = 20)
  @Column(name = "activation_key", length = 20)
  @JsonIgnore
  private String activationKey;

  @Size(max = 20)
  @Column(name = "reset_key", length = 20)
  @JsonIgnore
  private String resetKey;

  @Column(name = "reset_date")
  private Instant resetDate = null;

  @JsonIgnore
  @ManyToMany
  @JoinTable(
      name = "knowhow_user_authority",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")}
  )
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  @BatchSize(size = 20)
  private Set<Authority> authorities = new HashSet<>();

  // Domain methods for business logic

  /**
   * Activates the user account.
   *
   * @throws IllegalStateException if user is already activated
   */
  public void activate() {
    if (this.activated) {
      throw new IllegalStateException("User is already activated");
    }
    this.activated = true;
    this.activationKey = null;
  }

  /**
   * Deactivates the user account.
   *
   * @throws IllegalStateException if user is already deactivated
   */
  public void deactivate() {
    if (!this.activated) {
      throw new IllegalStateException("User is already deactivated");
    }
    this.activated = false;
  }

  /**
   * Initiates password reset process.
   *
   * @param resetKey the reset key to set
   * @throws IllegalArgumentException if resetKey is null or empty
   */
  public void initiatePasswordReset(String resetKey) {
    if (resetKey == null || resetKey.trim().isEmpty()) {
      throw new IllegalArgumentException("Reset key cannot be null or empty");
    }
    this.resetKey = resetKey;
    this.resetDate = Instant.now();
  }

  /**
   * Completes password reset process.
   *
   * @param newPassword the new password
   * @throws IllegalArgumentException if newPassword is null or empty
   * @throws IllegalStateException    if reset key is expired or not set
   */
  public void completePasswordReset(String newPassword) {
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("New password cannot be null or empty");
    }
    if (!isPasswordResetValid()) {
      throw new IllegalStateException("Password reset key is invalid or expired");
    }
    this.password = newPassword;
    this.resetKey = null;
    this.resetDate = null;
  }

  /**
   * Updates user profile information.
   *
   * @throws IllegalArgumentException if email format is invalid
   */
  public void updateProfile(String firstName, String lastName, String email, String langKey, String imageUrl) {
    this.firstName = firstName;
    this.lastName = lastName;
    if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new IllegalArgumentException("Invalid email format");
    }
    this.email = email;
    this.langKey = langKey;
    this.imageUrl = imageUrl;
  }

  /**
   * Changes user password.
   *
   * @param newPassword the new password
   * @throws IllegalArgumentException if newPassword is null or empty
   */
  public void changePassword(String newPassword) {
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("New password cannot be null or empty");
    }
    this.password = newPassword;
  }

  /**
   * Adds an authority to the user.
   *
   * @param authority the authority to add
   * @throws IllegalArgumentException if authority is null
   */
  public void addAuthority(Authority authority) {
    if (authority == null) {
      throw new IllegalArgumentException("Authority cannot be null");
    }
    this.authorities.add(authority);
  }

  /**
   * Removes an authority from the user.
   *
   * @param authority the authority to remove
   */
  public void removeAuthority(Authority authority) {
    this.authorities.remove(authority);
  }

  /**
   * Checks if user has a specific authority.
   *
   * @param authorityName the authority name to check
   * @return true if user has the authority
   */
  public boolean hasAuthority(String authorityName) {
    return authorities.stream()
        .anyMatch(authority -> authority.getName().equals(authorityName));
  }

  /**
   * Checks if password reset is valid (not expired).
   */
  public boolean isPasswordResetValid() {
    return resetKey != null && resetDate != null &&
           resetDate.isAfter(Instant.now().minusSeconds(86400)); // 24 hours
  }

  /**
   * Gets the full name of the user.
   *
   * @return concatenated first and last name
   */
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

  // Getters and setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public boolean isActivated() {
    return activated;
  }

  public void setActivated(boolean activated) {
    this.activated = activated;
  }

  public String getActivationKey() {
    return activationKey;
  }

  public void setActivationKey(String activationKey) {
    this.activationKey = activationKey;
  }

  public String getResetKey() {
    return resetKey;
  }

  public void setResetKey(String resetKey) {
    this.resetKey = resetKey;
  }

  public Instant getResetDate() {
    return resetDate;
  }

  public void setResetDate(Instant resetDate) {
    this.resetDate = resetDate;
  }

  public String getLangKey() {
    return langKey;
  }

  public void setLangKey(String langKey) {
    this.langKey = langKey;
  }

  public Set<Authority> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(Set<Authority> authorities) {
    this.authorities = authorities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }
    return id != null && id.equals(((User) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "User{" +
           "login='" + login + '\'' +
           ", firstName='" + firstName + '\'' +
           ", lastName='" + lastName + '\'' +
           ", email='" + email + '\'' +
           ", imageUrl='" + imageUrl + '\'' +
           ", activated='" + activated + '\'' +
           ", langKey='" + langKey + '\'' +
           ", activationKey='" + activationKey + '\'' +
           "}";
  }
}
