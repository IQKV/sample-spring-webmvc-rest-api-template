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

package com.iqkv.sample.webmvc.dashboard.usermanagement.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.boot.security.RandomUtil;
import com.iqkv.boot.security.SecurityUtils;
import com.iqkv.sample.webmvc.dashboard.config.AppConstants;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.Authority;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.UserDomainRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.UserDomainService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.infrastructure.AuthorityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for user management operations.
 * Orchestrates domain operations and publishes domain events.
 */
@Service
@Transactional
public class UserManagementService {

  private static final Logger LOG = LoggerFactory.getLogger(UserManagementService.class);

  private final UserDomainRepository userRepository;
  private final UserDomainService userDomainService;
  private final PasswordEncoder passwordEncoder;
  private final AuthorityRepository authorityRepository;
  private final ApplicationEventPublisher eventPublisher;

  public UserManagementService(
      UserDomainRepository userRepository,
      UserDomainService userDomainService,
      PasswordEncoder passwordEncoder,
      AuthorityRepository authorityRepository,
      ApplicationEventPublisher eventPublisher
  ) {
    this.userRepository = userRepository;
    this.userDomainService = userDomainService;
    this.passwordEncoder = passwordEncoder;
    this.authorityRepository = authorityRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Activates a user account using activation key.
   *
   * @param key the activation key
   * @return the activated user if found
   */
  public Optional<User> activateRegistration(String key) {
    LOG.debug("Activating user for activation key {}", key);
    return userRepository
        .findByActivationKey(key)
        .map(user -> {
          user.activate();
          User savedUser = userRepository.save(user);

          // Publish domain event
          eventPublisher.publishEvent(new UserActivatedEvent(
              savedUser.getId(),
              savedUser.getLogin(),
              savedUser.getEmail()
          ));

          LOG.debug("Activated user: {}", savedUser);
          return savedUser;
        });
  }

  /**
   * Completes password reset process.
   *
   * @param newPassword the new password
   * @param key         the reset key
   * @return the user if reset was successful
   */
  public Optional<User> completePasswordReset(String newPassword, String key) {
    LOG.debug("Reset user password for reset key {}", key);
    return userRepository
        .findByResetKey(key)
        .filter(user -> user.isPasswordResetValid())
        .map(user -> {
          String encodedPassword = passwordEncoder.encode(newPassword);
          user.completePasswordReset(encodedPassword);
          return userRepository.save(user);
        });
  }

  /**
   * Requests password reset for a user.
   *
   * @param email the user's email
   * @return the user if found and activated
   */
  public Optional<User> requestPasswordReset(String email) {
    return userRepository
        .findByEmail(email)
        .filter(User::isActivated)
        .map(user -> {
          String resetKey = RandomUtil.generateResetKey();
          user.initiatePasswordReset(resetKey);
          User savedUser = userRepository.save(user);

          // Publish domain event
          eventPublisher.publishEvent(new PasswordResetRequestedEvent(
              savedUser.getId(),
              savedUser.getEmail(),
              resetKey
          ));

          return savedUser;
        });
  }

  /**
   * Registers a new user.
   *
   * @param userDTO  the user data
   * @param password the password
   * @return the registered user
   */
  public User registerUser(AdminUserDTO userDTO, String password) {
    String login = userDTO.getLogin().toLowerCase();
    String email = userDTO.getEmail().toLowerCase();

    // Check for existing users and remove non-activated ones
    userRepository.findByLogin(login).ifPresent(existingUser -> {
      if (!removeNonActivatedUser(existingUser)) {
        throw new IllegalArgumentException("Login already exists: " + login);
      }
    });

    userRepository.findByEmail(email).ifPresent(existingUser -> {
      if (!removeNonActivatedUser(existingUser)) {
        throw new IllegalArgumentException("Email already exists: " + email);
      }
    });

    // Create new user
    User newUser = new User();
    String encryptedPassword = passwordEncoder.encode(password);
    newUser.setLogin(login);
    newUser.setPassword(encryptedPassword);
    newUser.setFirstName(userDTO.getFirstName());
    newUser.setLastName(userDTO.getLastName());
    newUser.setEmail(email);
    newUser.setImageUrl(userDTO.getImageUrl());
    newUser.setLangKey(userDTO.getLangKey());
    newUser.setActivated(false);

    String activationKey = RandomUtil.generateActivationKey();
    newUser.setActivationKey(activationKey);

    // Set default authority
    Set<Authority> authorities = new HashSet<>();
    authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
    newUser.setAuthorities(authorities);

    User savedUser = userRepository.save(newUser);

    // Publish domain event
    eventPublisher.publishEvent(new UserRegisteredEvent(
        savedUser.getId(),
        savedUser.getLogin(),
        savedUser.getEmail(),
        activationKey
    ));

    LOG.debug("Created Information for User: {}", savedUser);
    return savedUser;
  }

  /**
   * Creates a new user (admin operation).
   *
   * @param userDTO the user data
   * @return the created user
   */
  public User createUser(AdminUserDTO userDTO) {
    String login = userDTO.getLogin().toLowerCase();
    String email = userDTO.getEmail() != null ? userDTO.getEmail().toLowerCase() : null;

    // Validate uniqueness
    userDomainService.validateUniqueLogin(login);
    if (email != null) {
      userDomainService.validateUniqueEmail(email);
    }

    User user = new User();
    user.setLogin(login);
    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setEmail(email);
    user.setImageUrl(userDTO.getImageUrl());
    user.setLangKey(userDTO.getLangKey() != null ? userDTO.getLangKey() : AppConstants.DEFAULT_LANGUAGE);

    String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
    user.setPassword(encryptedPassword);
    user.setResetKey(RandomUtil.generateResetKey());
    user.setResetDate(Instant.now());
    user.setActivated(true);

    if (userDTO.getAuthorities() != null) {
      Set<Authority> authorities = new HashSet<>();
      userDTO.getAuthorities().forEach(authorityName ->
          authorityRepository.findById(authorityName).ifPresent(authorities::add)
      );
      user.setAuthorities(authorities);
    }

    User savedUser = userRepository.save(user);
    LOG.debug("Created Information for User: {}", savedUser);
    return savedUser;
  }

  /**
   * Updates user information (admin operation).
   *
   * @param userDTO the user data
   * @return the updated user DTO
   */
  public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
    return userRepository.findById(userDTO.getId())
        .map(user -> {
          String login = userDTO.getLogin().toLowerCase();
          String email = userDTO.getEmail() != null ? userDTO.getEmail().toLowerCase() : null;

          // Validate uniqueness for other users
          userDomainService.validateUniqueLoginForUser(login, user.getId());
          if (email != null) {
            userDomainService.validateUniqueEmailForUser(email, user.getId());
          }

          user.setLogin(login);
          user.setFirstName(userDTO.getFirstName());
          user.setLastName(userDTO.getLastName());
          user.setEmail(email);
          user.setImageUrl(userDTO.getImageUrl());
          user.setActivated(userDTO.isActivated());
          user.setLangKey(userDTO.getLangKey());

          // Update authorities
          Set<Authority> managedAuthorities = user.getAuthorities();
          managedAuthorities.clear();
          userDTO.getAuthorities().forEach(authorityName ->
              authorityRepository.findById(authorityName).ifPresent(managedAuthorities::add)
          );

          User savedUser = userRepository.save(user);
          LOG.debug("Changed Information for User: {}", savedUser);
          return savedUser;
        })
        .map(AdminUserDTO::new);
  }

  /**
   * Updates current user's profile.
   *
   * @param firstName first name
   * @param lastName  last name
   * @param email     email
   * @param langKey   language key
   * @param imageUrl  image URL
   */
  public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
    SecurityUtils.getCurrentUserLogin()
        .flatMap(userRepository::findByLogin)
        .ifPresent(user -> {
          user.updateProfile(firstName, lastName, email, langKey, imageUrl);
          userRepository.save(user);
          LOG.debug("Changed Information for User: {}", user);
        });
  }

  /**
   * Deletes a user.
   *
   * @param login the user login
   */
  public void deleteUser(String login) {
    userRepository.findByLogin(login)
        .ifPresent(user -> {
          userRepository.delete(user);
          LOG.debug("Deleted User: {}", user);
        });
  }

  /**
   * Changes current user's password.
   *
   * @param currentClearTextPassword current password
   * @param newPassword              new password
   */
  public void changePassword(String currentClearTextPassword, String newPassword) {
    SecurityUtils.getCurrentUserLogin()
        .flatMap(userRepository::findByLogin)
        .ifPresent(user -> {
          String currentEncryptedPassword = user.getPassword();
          if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
            throw new IllegalArgumentException("Invalid current password");
          }
          String encryptedPassword = passwordEncoder.encode(newPassword);
          user.changePassword(encryptedPassword);
          userRepository.save(user);
          LOG.debug("Changed password for User: {}", user);
        });
  }

  /**
   * Gets all managed users with pagination.
   *
   * @param pageable pagination information
   * @return page of user DTOs
   */
  @Transactional(readOnly = true)
  public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(AdminUserDTO::new);
  }

  /**
   * Gets all public users with pagination.
   *
   * @param pageable pagination information
   * @return page of user DTOs
   */
  @Transactional(readOnly = true)
  public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
    return userRepository.findAllActivated(pageable).map(UserDTO::new);
  }

  /**
   * Gets user with authorities by login.
   *
   * @param login the login
   * @return optional user
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserWithAuthoritiesByLogin(String login) {
    return userRepository.findWithAuthoritiesByLogin(login);
  }

  /**
   * Gets user with authorities by email.
   *
   * @param email the email
   * @return optional user
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserWithAuthoritiesByEmail(String email) {
    return userRepository.findWithAuthoritiesByEmail(email);
  }

  /**
   * Gets current user with authorities.
   *
   * @return optional user
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserWithAuthorities() {
    return SecurityUtils.getCurrentUserLogin()
        .flatMap(userRepository::findWithAuthoritiesByLogin);
  }

  /**
   * Counts total number of users.
   *
   * @return total user count
   */
  @Transactional(readOnly = true)
  public long countUsers() {
    return userRepository.count();
  }

  /**
   * Removes non-activated users older than 3 days.
   * Scheduled to run daily at 01:00.
   */
  @Scheduled(cron = "0 0 1 * * ?")
  public void removeNotActivatedUsers() {
    userRepository
        .findAllNonActivatedBefore(Instant.now().minus(3, ChronoUnit.DAYS))
        .forEach(user -> {
          LOG.debug("Deleting not activated user {}", user.getLogin());
          userRepository.delete(user);
        });
  }

  /**
   * Gets all authorities.
   *
   * @return list of authority names
   */
  @Transactional(readOnly = true)
  public List<String> getAuthorities() {
    return authorityRepository.findAll().stream()
        .map(Authority::getName)
        .toList();
  }

  private boolean removeNonActivatedUser(User existingUser) {
    if (existingUser.isActivated()) {
      return false;
    }
    userRepository.delete(existingUser);
    return true;
  }
}
