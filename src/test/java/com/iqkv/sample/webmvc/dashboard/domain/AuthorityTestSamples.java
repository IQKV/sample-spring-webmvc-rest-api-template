/*
 * Copyright 2025 IQKV Team.
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

package com.iqkv.sample.webmvc.dashboard.domain;

import java.util.UUID;

public class AuthorityTestSamples {

  public static Authority getAuthoritySample1() {
    return new Authority().name("name1");
  }

  public static Authority getAuthoritySample2() {
    return new Authority().name("name2");
  }

  public static Authority getAuthorityRandomSampleGenerator() {
    return new Authority().name(UUID.randomUUID().toString());
  }
}
