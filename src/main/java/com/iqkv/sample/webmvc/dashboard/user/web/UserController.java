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

package com.iqkv.sample.webmvc.dashboard.user.web;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iqkv.sample.webmvc.dashboard.user.domain.User;
import com.iqkv.sample.webmvc.dashboard.user.service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.LoggerFactory;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  public UserController(@Qualifier("userModuleService") UserService userService) {
    this.userService = userService;
  }

  /**
   * GET /api/users : get all users.
   *
   * @param pageable the pagination information
   * @return the ResponseEntity with status 200 (OK) and with body all users
   */
  @GetMapping
  public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
    log.debug("REST request to get all Users");
    Page<User> page = userService.getAllUsers(pageable);
    return ResponseEntity.ok(page);
  }

  /**
   * GET /api/users/:login : get the "login" user.
   *
   * @param login the login of the user to find
   * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
   */
  @GetMapping("/{login}")
  public ResponseEntity<User> getUser(@PathVariable String login) {
    log.debug("REST request to get User : {}", login);
    Optional<User> user = userService.getUserByLogin(login);
    return user.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * POST /api/users : create a new user.
   *
   * @param user the user to create
   * @return the ResponseEntity with status 201 (Created) and with body the new user
   */
  @PostMapping
  public ResponseEntity<User> createUser(@RequestBody User user) {
    log.debug("REST request to save User : {}", user);
    User result = userService.createUser(user.getLogin(), user.getFirstName(), user.getLastName(), user.getEmail());
    return ResponseEntity.ok(result);
  }

  /**
   * DELETE /api/users/:login : delete the "login" User.
   *
   * @param login the login of the user to delete
   * @return the ResponseEntity with status 204 (NO_CONTENT)
   */
  @DeleteMapping("/{login}")
  public ResponseEntity<Void> deleteUser(@PathVariable String login) {
    log.debug("REST request to delete User: {}", login);
    userService.deleteUser(login);
    return ResponseEntity.noContent().build();
  }
}