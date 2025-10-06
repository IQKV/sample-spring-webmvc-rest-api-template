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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.config.AppConstants;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing users in the User Management bounded context.
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

  private static final Logger LOG = LoggerFactory.getLogger(UserManagementController.class);
  private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
      Arrays.asList(
          "id",
          "login",
          "firstName",
          "lastName",
          "email",
          "activated",
          "langKey",
          "createdBy",
          "createdDate",
          "lastModifiedBy",
          "lastModifiedDate"
      )
  );

  private final UserManagementService userManagementService;

  public UserManagementController(UserManagementService userManagementService) {
    this.userManagementService = userManagementService;
  }

  /**
   * Creates a new user.
   *
   * @param userDTO the user to create
   * @return the ResponseEntity with status 201 (Created) and the new user in body
   * @throws URISyntaxException if the Location URI syntax is incorrect
   */
  @PostMapping
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<User> createUser(@Valid @RequestBody AdminUserDTO userDTO) throws URISyntaxException {
    LOG.debug("REST request to save User : {}", userDTO);

    if (userDTO.getId() != null) {
      throw new IllegalArgumentException("A new user cannot already have an ID");
    }
    if (userDTO.getLogin() == null) {
      throw new IllegalArgumentException("Login cannot be null");
    }

    User newUser = userManagementService.createUser(userDTO);
    return ResponseEntity.created(new URI("/api/admin/users/" + newUser.getLogin()))
        .body(newUser);
  }

  /**
   * Updates an existing user.
   *
   * @param userDTO the user to update
   * @return the ResponseEntity with status 200 (OK) and the updated user in body
   */
  @PutMapping
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<AdminUserDTO> updateUser(@Valid @RequestBody AdminUserDTO userDTO) {
    LOG.debug("REST request to update User : {}", userDTO);

    Optional<AdminUserDTO> updatedUser = userManagementService.updateUser(userDTO);

    return updatedUser
        .map(user -> ResponseEntity.ok().body(user))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Gets all users with pagination and sorting.
   *
   * @param pageable the pagination information
   * @return the ResponseEntity with status 200 (OK) and the list of users in body
   */
  @GetMapping
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<List<AdminUserDTO>> getAllUsers(Pageable pageable) {
    LOG.debug("REST request to get all Users for an admin");

    if (!onlyContainsAllowedProperties(pageable)) {
      return ResponseEntity.badRequest().build();
    }

    final Page<AdminUserDTO> page = userManagementService.getAllManagedUsers(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
    return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
  }

  /**
   * Gets a user by login.
   *
   * @param login the login of the user to find
   * @return the ResponseEntity with status 200 (OK) and the user in body
   */
  @GetMapping("/{login}")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<AdminUserDTO> getUser(@PathVariable @Pattern(regexp = AppConstants.LOGIN_REGEX) String login) {
    LOG.debug("REST request to get User : {}", login);
    return userManagementService
        .getUserWithAuthoritiesByLogin(login)
        .map(AdminUserDTO::new)
        .map(userDTO -> ResponseEntity.ok().body(userDTO))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Deletes a user.
   *
   * @param login the login of the user to delete
   * @return the ResponseEntity with status 204 (NO_CONTENT)
   */
  @DeleteMapping("/{login}")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Void> deleteUser(@PathVariable @Pattern(regexp = AppConstants.LOGIN_REGEX) String login) {
    LOG.debug("REST request to delete User: {}", login);
    userManagementService.deleteUser(login);
    return ResponseEntity.noContent().build();
  }

  /**
   * Gets all authorities.
   *
   * @return the ResponseEntity with status 200 (OK) and the list of authorities in body
   */
  @GetMapping("/authorities")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public List<String> getAuthorities() {
    return userManagementService.getAuthorities();
  }

  private boolean onlyContainsAllowedProperties(Pageable pageable) {
    return pageable.getSort().stream()
        .map(Sort.Order::getProperty)
        .allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
  }
}
