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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for public user information.
 */
@RestController
@RequestMapping("/api/users")
public class PublicUserController {

  private static final Logger LOG = LoggerFactory.getLogger(PublicUserController.class);
  private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
      Arrays.asList("id", "login", "firstName", "lastName", "email", "langKey")
  );

  private final UserManagementService userManagementService;

  public PublicUserController(UserManagementService userManagementService) {
    this.userManagementService = userManagementService;
  }

  /**
   * Gets all public users with pagination and sorting.
   *
   * @param pageable the pagination information
   * @return the ResponseEntity with status 200 (OK) and the list of users in body
   */
  @GetMapping
  public ResponseEntity<List<UserDTO>> getAllPublicUsers(Pageable pageable) {
    LOG.debug("REST request to get all public User names");

    if (!onlyContainsAllowedProperties(pageable)) {
      return ResponseEntity.badRequest().build();
    }

    final Page<UserDTO> page = userManagementService.getAllPublicUsers(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
    return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
  }

  private boolean onlyContainsAllowedProperties(Pageable pageable) {
    return pageable.getSort().stream()
        .map(Sort.Order::getProperty)
        .allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
  }
}
