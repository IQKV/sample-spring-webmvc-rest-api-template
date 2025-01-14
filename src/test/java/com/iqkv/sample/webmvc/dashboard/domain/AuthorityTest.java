/*
 * Copyright 2024 IQKV Team.
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

import static com.iqkv.sample.webmvc.dashboard.domain.AuthorityTestSamples.getAuthoritySample1;
import static com.iqkv.sample.webmvc.dashboard.domain.AuthorityTestSamples.getAuthoritySample2;
import static org.assertj.core.api.Assertions.assertThat;

import com.iqkv.sample.webmvc.dashboard.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuthorityTest {

  @Test
  void equalsVerifier() throws Exception {
    TestUtil.equalsVerifier(Authority.class);
    Authority authority1 = getAuthoritySample1();
    Authority authority2 = new Authority();
    assertThat(authority1).isNotEqualTo(authority2);

    authority2.setName(authority1.getName());
    assertThat(authority1).isEqualTo(authority2);

    authority2 = getAuthoritySample2();
    assertThat(authority1).isNotEqualTo(authority2);
  }

  @Test
  void hashCodeVerifier() {
    Authority authority = new Authority();
    assertThat(authority.hashCode()).isZero();

    Authority authority1 = getAuthoritySample1();
    authority.setName(authority1.getName());
    assertThat(authority).hasSameHashCodeAs(authority1);
  }
}
