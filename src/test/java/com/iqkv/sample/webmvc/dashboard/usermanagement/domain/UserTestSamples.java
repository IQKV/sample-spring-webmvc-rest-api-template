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

import java.util.Set;

import com.iqkv.boot.security.AuthoritiesConstants;

/**
 * Test data factory for User domain objects.
 */
public class UserTestSamples {

  public static User getUserSample1() {
    User user = new User();
    user.setId(1L);
    user.setLogin("testuser1");
    user.setEmail("test1@example.com");
    user.setFirstName("Test");
    user.setLastName("User1");
    user.setActivated(true);
    user.setLangKey("en");
    return user;
  }

  public static User getUserSample2() {
    User user = new User();
    user.setId(2L);
    user.setLogin("testuser2");
    user.setEmail("test2@example.com");
    user.setFirstName("Test");
    user.setLastName("User2");
    user.setActivated(false);
    user.setLangKey("fr");
    return user;
  }

  public static User getActivatedUserWithAuthorities() {
    User user = getUserSample1();

    Authority userAuthority = new Authority();
    userAuthority.setName(AuthoritiesConstants.USER);

    Authority adminAuthority = new Authority();
    adminAuthority.setName(AuthoritiesConstants.ADMIN);

    user.setAuthorities(Set.of(userAuthority, adminAuthority));
    return user;
  }

  public static User getNonActivatedUser() {
    User user = getUserSample2();
    user.setActivationKey("activation-key-123");
    return user;
  }

  public static User getUserWithResetKey() {
    User user = getUserSample1();
    user.setResetKey("reset-key-123");
    return user;
  }
}
