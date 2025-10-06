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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.iqkv.boot.security.AuthoritiesConstants;
import org.springframework.stereotype.Service;

/**
 * Domain service for user-related business logic that doesn't naturally fit
 * within a single aggregate or involves multiple aggregates.
 * <p>
 * This service encapsulates complex business rules and cross-aggregate operations
 * within the User Management bounded context.
 */
@Service
public class UserDomainService {

  private static final Pattern STRONG_PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final int ACCOUNT_LOCKOUT_DURATION_HOURS = 24;
  private static final int PASSWORD_RESET_VALIDITY_HOURS = 24;

  private final UserDomainRepository userRepository;

  public UserDomainService(UserDomainRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Validates that a login is unique.
   *
   * @param login the login to validate
   * @throws IllegalArgumentException if login already exists
   */
  public void validateUniqueLogin(String login) {
    if (userRepository.existsByLogin(login.toLowerCase())) {
      throw new IllegalArgumentException("Login already exists: " + login);
    }
  }

  /**
   * Validates that an email is unique.
   *
   * @param email the email to validate
   * @throws IllegalArgumentException if email already exists
   */
  public void validateUniqueEmail(String email) {
    if (userRepository.existsByEmail(email.toLowerCase())) {
      throw new IllegalArgumentException("Email already exists: " + email);
    }
  }

  /**
   * Validates that a login is unique for a specific user (excluding the user itself).
   *
   * @param login  the login to validate
   * @param userId the user ID to exclude from validation
   * @throws IllegalArgumentException if login already exists for another user
   */
  public void validateUniqueLoginForUser(String login, Long userId) {
    userRepository.findByLogin(login.toLowerCase())
        .filter(user -> !user.getId().equals(userId))
        .ifPresent(user -> {
          throw new IllegalArgumentException("Login already exists: " + login);
        });
  }

  /**
   * Validates that an email is unique for a specific user (excluding the user itself).
   *
   * @param email  the email to validate
   * @param userId the user ID to exclude from validation
   * @throws IllegalArgumentException if email already exists for another user
   */
  public void validateUniqueEmailForUser(String email, Long userId) {
    userRepository.findByEmail(email.toLowerCase())
        .filter(user -> !user.getId().equals(userId))
        .ifPresent(user -> {
          throw new IllegalArgumentException("Email already exists: " + email);
        });
  }

  /**
   * Validates password strength according to business rules.
   *
   * @param password the password to validate
   * @throws IllegalArgumentException if password doesn't meet strength requirements
   */
  public void validatePasswordStrength(String password) {
    if (password == null || password.length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }

    if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
      throw new IllegalArgumentException(
          "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
      );
    }
  }

  /**
   * Validates user registration data according to business rules.
   *
   * @param login    the login to validate
   * @param email    the email to validate
   * @param password the password to validate
   * @throws IllegalArgumentException if any validation fails
   */
  public void validateUserRegistration(String login, String email, String password) {
    validateUniqueLogin(login);
    validateUniqueEmail(email);
    validatePasswordStrength(password);

    // Additional business rules
    if (Email.of(email).isDisposable()) {
      throw new IllegalArgumentException("Disposable email addresses are not allowed");
    }
  }

  /**
   * Determines if a user can be activated based on business rules.
   *
   * @param user the user to check
   * @return true if the user can be activated
   */
  public boolean canActivateUser(User user) {
    return user != null &&
           !user.isActivated() &&
           user.getActivationKey() != null &&
           user.getEmail() != null;
  }

  /**
   * Determines if a user can reset their password.
   *
   * @param user the user to check
   * @return true if the user can reset password
   */
  public boolean canResetPassword(User user) {
    return user != null &&
           user.isActivated() &&
           user.getEmail() != null;
  }

  /**
   * Checks if a password reset request is still valid.
   *
   * @param user the user with reset request
   * @return true if the reset request is valid
   */
  public boolean isPasswordResetValid(User user) {
    if (user == null || user.getResetKey() == null || user.getResetDate() == null) {
      return false;
    }

    Instant expiryTime = user.getResetDate().plus(PASSWORD_RESET_VALIDITY_HOURS, ChronoUnit.HOURS);
    return Instant.now().isBefore(expiryTime);
  }

  /**
   * Determines the appropriate authorities for a new user based on business rules.
   *
   * @param email the user's email
   * @return set of authorities to assign
   */
  public Set<Authority> determineDefaultAuthorities(String email) {
    // Business rule: Admin emails get admin role
    if (isAdminEmail(email)) {
      return Set.of(
          Authority.of(AuthoritiesConstants.ADMIN),
          Authority.of(AuthoritiesConstants.USER)
      );
    }

    // Default: Regular user role
    return Set.of(Authority.of(AuthoritiesConstants.USER));
  }

  /**
   * Validates if a user can be assigned specific authorities.
   *
   * @param user        the user
   * @param authorities the authorities to assign
   * @param assignedBy  the user making the assignment
   * @throws IllegalArgumentException if assignment is not allowed
   */
  public void validateAuthorityAssignment(User user, Set<Authority> authorities, User assignedBy) {
    if (user == null || authorities == null || assignedBy == null) {
      throw new IllegalArgumentException("User, authorities, and assignedBy cannot be null");
    }

    // Business rule: Only admins can assign admin role
    boolean hasAdminRole = authorities.stream()
        .anyMatch(auth -> AuthoritiesConstants.ADMIN.equals(auth.getName()));

    if (hasAdminRole && !assignedBy.hasAuthority(AuthoritiesConstants.ADMIN)) {
      throw new IllegalArgumentException("Only administrators can assign admin role");
    }

    // Business rule: Users cannot modify their own admin status
    if (user.getId().equals(assignedBy.getId()) && hasAdminRole) {
      throw new IllegalArgumentException("Users cannot modify their own admin status");
    }
  }

  /**
   * Finds users who need account activation reminders.
   *
   * @param daysSinceRegistration number of days since registration
   * @return list of users needing reminders
   */
  public List<User> findUsersNeedingActivationReminder(int daysSinceRegistration) {
    Instant cutoffDate = Instant.now().minus(daysSinceRegistration, ChronoUnit.DAYS);
    return userRepository.findUnactivatedUsersSince(cutoffDate);
  }

  /**
   * Finds users with expired password reset requests.
   *
   * @return list of users with expired reset requests
   */
  public List<User> findUsersWithExpiredPasswordReset() {
    Instant expiryTime = Instant.now().minus(PASSWORD_RESET_VALIDITY_HOURS, ChronoUnit.HOURS);
    return userRepository.findUsersWithExpiredPasswordReset(expiryTime);
  }

  /**
   * Validates if a user profile update is allowed.
   *
   * @param user      the user being updated
   * @param updatedBy the user making the update
   * @param newEmail  the new email (if changing)
   * @throws IllegalArgumentException if update is not allowed
   */
  public void validateProfileUpdate(User user, User updatedBy, String newEmail) {
    if (user == null || updatedBy == null) {
      throw new IllegalArgumentException("User and updatedBy cannot be null");
    }

    // Business rule: Users can only update their own profile unless they're admin
    if (!user.getId().equals(updatedBy.getId()) &&
        !updatedBy.hasAuthority(AuthoritiesConstants.ADMIN)) {
      throw new IllegalArgumentException("Users can only update their own profile");
    }

    // Validate email uniqueness if changing
    if (newEmail != null && !newEmail.equals(user.getEmail())) {
      validateUniqueEmailForUser(newEmail, user.getId());
    }
  }

  /**
   * Calculates user account status based on business rules.
   *
   * @param user the user to check
   * @return account status information
   */
  public UserAccountStatus calculateAccountStatus(User user) {
    if (user == null) {
      return UserAccountStatus.INVALID;
    }

    if (!user.isActivated()) {
      return UserAccountStatus.PENDING_ACTIVATION;
    }

    if (user.getResetKey() != null && isPasswordResetValid(user)) {
      return UserAccountStatus.PENDING_PASSWORD_RESET;
    }

    return UserAccountStatus.ACTIVE;
  }

  // Private helper methods

  private boolean isAdminEmail(String email) {
    // Business rule: Emails from specific domains get admin access
    String domain = Email.of(email).getDomain();
    return domain != null && (
        domain.equals("admin.company.com") ||
        domain.equals("management.company.com")
    );
  }

  /**
   * Enum representing user account status.
   */
  public enum UserAccountStatus {
    ACTIVE,
    PENDING_ACTIVATION,
    PENDING_PASSWORD_RESET,
    INVALID
  }
}
