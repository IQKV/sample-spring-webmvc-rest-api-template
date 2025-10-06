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
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.iqkv.boot.security.AuthoritiesConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.domain.Persistable;
import org.springframework.util.StringUtils;

/**
 * Authority domain entity representing a user role or permission within the User Management bounded context.
 * <p>
 * This entity encapsulates the business rules and validation logic for user authorities,
 * ensuring proper role hierarchy and security constraints are maintained.
 */
@Entity
@Table(name = "knowhow_authority")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties(value = {"new", "id"})
public class Authority implements Serializable, Persistable<String> {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final Set<String> SYSTEM_AUTHORITIES = Set.of(
      AuthoritiesConstants.ADMIN,
      AuthoritiesConstants.USER
  );

  @NotNull
  @Size(max = 50)
  @Id
  @Column(name = "name", length = 50, nullable = false)
  private String name;

  @Transient
  private boolean isPersisted;

  public String getName() {
    return this.name;
  }

  public Authority name(String name) {
    this.setName(name);
    return this;
  }

  public void setName(String name) {
    validateAuthorityName(name);
    this.name = name;
  }

  @PostLoad
  @PostPersist
  public void updateEntityState() {
    this.setIsPersisted();
  }

  @Override
  public String getId() {
    return this.name;
  }

  @Transient
  @Override
  public boolean isNew() {
    return !this.isPersisted;
  }

  public Authority setIsPersisted() {
    this.isPersisted = true;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Authority)) {
      return false;
    }
    return getName() != null && getName().equals(((Authority) o).getName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName());
  }

  @Override
  public String toString() {
    return "Authority{" +
           "name=" + getName() +
           "}";
  }

  // Domain Logic Methods

  /**
   * Validates the authority name according to business rules.
   *
   * @param name the authority name to validate
   * @throws IllegalArgumentException if the name is invalid
   */
  private void validateAuthorityName(String name) {
    if (!StringUtils.hasText(name)) {
      throw new IllegalArgumentException("Authority name cannot be null or empty");
    }

    if (!name.startsWith("ROLE_")) {
      throw new IllegalArgumentException("Authority name must start with ROLE_");
    }
  }

  /**
   * Checks if this authority is valid according to business rules.
   *
   * @return true if the authority is valid
   */
  public boolean isValid() {
    return StringUtils.hasText(name) && name.startsWith("ROLE_");
  }

  /**
   * Checks if this is a system-defined authority.
   *
   * @return true if this is a system authority
   */
  public boolean isSystemAuthority() {
    return SYSTEM_AUTHORITIES.contains(name);
  }

  /**
   * Gets the authority level for hierarchy comparison.
   * Higher numbers indicate higher authority levels.
   *
   * @return the authority level
   */
  public int getLevel() {
    return switch (name) {
      case AuthoritiesConstants.ADMIN -> 10;
      case AuthoritiesConstants.USER -> 1;
      default -> 5; // Custom authorities get medium level
    };
  }

  /**
   * Checks if this authority has a higher level than another authority.
   *
   * @param other the authority to compare with
   * @return true if this authority has higher level
   */
  public boolean hasHigherLevelThan(Authority other) {
    return this.getLevel() > other.getLevel();
  }

  /**
   * Gets a human-readable display name for the authority.
   *
   * @return the display name
   */
  public String getDisplayName() {
    if (name == null) {
      return "";
    }

    String displayName = name.replace("ROLE_", "").replace("_", " ");
    return capitalizeWords(displayName);
  }

  /**
   * Creates an Authority instance from a string name.
   *
   * @param authorityName the authority name
   * @return a new Authority instance
   */
  public static Authority of(String authorityName) {
    Authority authority = new Authority();
    authority.setName(authorityName);
    return authority;
  }

  private String capitalizeWords(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    String[] words = str.toLowerCase().split(" ");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < words.length; i++) {
      if (i > 0) {
        result.append(" ");
      }
      if (!words[i].isEmpty()) {
        result.append(Character.toUpperCase(words[i].charAt(0)))
            .append(words[i].substring(1));
      }
    }

    return result.toString();
  }
}
