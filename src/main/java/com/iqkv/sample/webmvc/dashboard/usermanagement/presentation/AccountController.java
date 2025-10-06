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

package com.iqkv.sample.webmvc.dashboard.usermanagement.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;

import com.iqkv.boot.security.SecurityUtils;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.presentation.vm.KeyAndPasswordVM;
import com.iqkv.sample.webmvc.dashboard.usermanagement.presentation.vm.ManagedUserVM;
import com.iqkv.sample.webmvc.dashboard.usermanagement.presentation.vm.PasswordChangeVM;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountController {

  private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

  private final UserManagementService userManagementService;

  public AccountController(UserManagementService userManagementService) {
    this.userManagementService = userManagementService;
  }

  /**
   * Registers a new user account.
   *
   * @param managedUserVM the managed user View Model
   * @param request       the HTTP request
   */
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM, HttpServletRequest request) {
    if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
      throw new IllegalArgumentException("Invalid password");
    }

    AdminUserDTO userDTO = new AdminUserDTO();
    userDTO.setLogin(managedUserVM.getLogin().toLowerCase());
    userDTO.setFirstName(managedUserVM.getFirstName());
    userDTO.setLastName(managedUserVM.getLastName());
    userDTO.setEmail(managedUserVM.getEmail().toLowerCase());
    userDTO.setImageUrl(managedUserVM.getImageUrl());
    userDTO.setLangKey(managedUserVM.getLangKey());

    User user = userManagementService.registerUser(userDTO, managedUserVM.getPassword());
    LOG.debug("Created user: {}", user);
  }

  /**
   * Activates a user account.
   *
   * @param key the activation key
   */
  @GetMapping("/activate")
  public void activateAccount(@RequestParam(value = "key") String key) {
    Optional<User> user = userManagementService.activateRegistration(key);
    if (user.isEmpty()) {
      throw new IllegalArgumentException("No user was found for this activation key");
    }
  }

  /**
   * Gets the current user account.
   *
   * @return the current user
   */
  @GetMapping("/account")
  public AdminUserDTO getAccount() {
    return userManagementService
        .getUserWithAuthorities()
        .map(AdminUserDTO::new)
        .orElseThrow(() -> new IllegalArgumentException("User could not be found"));
  }

  /**
   * Saves the current user account.
   *
   * @param userDTO the current user information
   */
  @PostMapping("/account")
  public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
    String userLogin = SecurityUtils.getCurrentUserLogin()
        .orElseThrow(() -> new IllegalArgumentException("Current user login not found"));

    Optional<User> existingUser = userManagementService.getUserWithAuthoritiesByLogin(userLogin);
    if (existingUser.isEmpty()) {
      throw new IllegalArgumentException("User could not be found");
    }

    userManagementService.updateUser(
        userDTO.getFirstName(),
        userDTO.getLastName(),
        userDTO.getEmail(),
        userDTO.getLangKey(),
        userDTO.getImageUrl()
    );
  }

  /**
   * Changes the current user's password.
   *
   * @param passwordChangeVM the password change information
   */
  @PostMapping(path = "/account/change-password")
  public void changePassword(@RequestBody PasswordChangeVM passwordChangeVM) {
    String currentPassword = passwordChangeVM.getCurrentPassword();
    if (isPasswordLengthInvalid(currentPassword) || isPasswordLengthInvalid(passwordChangeVM.getNewPassword())) {
      throw new IllegalArgumentException("Invalid password");
    }
    userManagementService.changePassword(currentPassword, passwordChangeVM.getNewPassword());
  }

  /**
   * Requests a password reset.
   *
   * @param mail the email of the user
   */
  @PostMapping(path = "/account/reset-password/init")
  public void requestPasswordReset(@RequestBody String mail) {
    Optional<User> user = userManagementService.requestPasswordReset(mail);
    if (user.isEmpty()) {
      LOG.warn("Password reset requested for non existing mail");
      // Pretend the request has been successful to prevent checking which emails really exist
      // but log that an invalid attempt has been made
    }
  }

  /**
   * Finishes the password reset process.
   *
   * @param keyAndPassword the generated key and the new password
   */
  @PostMapping(path = "/account/reset-password/finish")
  public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
    if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
      throw new IllegalArgumentException("Invalid password");
    }
    Optional<User> user = userManagementService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

    if (user.isEmpty()) {
      throw new IllegalArgumentException("No user was found for this reset key");
    }
  }

  private static boolean isPasswordLengthInvalid(String password) {
    return (
        StringUtils.isEmpty(password) ||
        password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
        password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
    );
  }
}
