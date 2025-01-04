/*
 * Copyright 2024 IQKV.
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

package com.iqkv.boot.security.jwt;

import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.BEARER;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createExpiredToken;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createInvalidToken;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createSignedInvalidJwt;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createTokenWithDifferentSignature;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createValidToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Collection;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@AuthenticationIntegrationTest
class TokenAuthenticationSecurityMetersIT {

  private static final String INVALID_TOKENS_METER_EXPECTED_NAME = "security.authentication.invalid-tokens";

  @Autowired
  private MockMvc mvc;

  @Value("${iqkv.security.authentication.jwt.base64-secret}")
  private String jwtKey;

  @Autowired
  private MeterRegistry meterRegistry;

  @Test
  void testValidTokenShouldNotCountAnything() throws Exception {
    Collection<Counter> counters = meterRegistry.find(INVALID_TOKENS_METER_EXPECTED_NAME).counters();

    var count = aggregate(counters);

    tryToAuthenticate(createValidToken(jwtKey));

    assertThat(aggregate(counters)).isEqualTo(count);
  }

  @Test
  void testTokenExpiredCount() throws Exception {
    var count = meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "expired").counter().count();

    tryToAuthenticate(createExpiredToken(jwtKey));

    assertThat(meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "expired").counter().count()).isEqualTo(count + 1);
  }

  @Test
  void testTokenSignatureInvalidCount() throws Exception {
    var count = meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "invalid-signature").counter().count();

    tryToAuthenticate(createTokenWithDifferentSignature());

    assertThat(meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "invalid-signature").counter().count()).isEqualTo(
        count + 1
    );
  }

  @Test
  void testTokenMalformedCount() throws Exception {
    var count = meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "malformed").counter().count();

    tryToAuthenticate(createSignedInvalidJwt(jwtKey));

    assertThat(meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "malformed").counter().count()).isEqualTo(count + 1);
  }

  @Test
  void testTokenInvalidCount() throws Exception {
    var count = meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "malformed").counter().count();

    tryToAuthenticate(createInvalidToken(jwtKey));

    assertThat(meterRegistry.get(INVALID_TOKENS_METER_EXPECTED_NAME).tag("cause", "malformed").counter().count()).isEqualTo(count + 1);
  }

  private void tryToAuthenticate(String token) throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/authenticate").header(AUTHORIZATION, BEARER + token));
  }

  private double aggregate(Collection<Counter> counters) {
    return counters.stream().mapToDouble(Counter::count).sum();
  }
}
