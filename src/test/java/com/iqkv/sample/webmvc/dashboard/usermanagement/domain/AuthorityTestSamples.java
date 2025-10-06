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

import com.iqkv.boot.security.AuthoritiesConstants;

/**
 * Test samples for Authority domain entity.
 * <p>
 * Provides pre-configured Authority instances for testing purposes
 * within the User Management bounded context.
 */
public class AuthorityTestSamples {

  public static Authority getAuthoritySample1() {
    return Authority.of(AuthoritiesConstants.USER);
  }

  public static Authority getAuthoritySample2() {
    return Authority.of(AuthoritiesConstants.ADMIN);
  }

  public static Authority getCustomAuthoritySample() {
    return Authority.of("ROLE_MANAGER");
  }

  public static Authority getInvalidAuthoritySample() {
    Authority authority = new Authority();
    // Note: This will throw exception when setName is called
    return authority;
  }

  public static Authority createAuthorityWithName(String name) {
    return Authority.of(name);
  }
}
