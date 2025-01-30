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

package com.iqkv.boot.security.jwt;

import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.BEARER;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createExpiredToken;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createSignedInvalidJwt;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createTokenWithDifferentSignature;
import static com.iqkv.boot.security.jwt.JwtAuthenticationTestUtils.createValidToken;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@AuthenticationIntegrationTest
class TokenAuthenticationIT {

  @Autowired
  private MockMvc mvc;

  @Value("${iqkv.security.authentication.jwt.base64-secret}")
  private String jwtKey;

  @Test
  void testLoginWithValidToken() throws Exception {
    expectOk(createValidToken(jwtKey));
  }

  @Test
  void testReturnFalseWhenJWThasInvalidSignature() throws Exception {
    expectUnauthorized(createTokenWithDifferentSignature());
  }

  @Test
  void testReturnFalseWhenJWTisMalformed() throws Exception {
    expectUnauthorized(createSignedInvalidJwt(jwtKey));
  }

  @Test
  void testReturnFalseWhenJWTisExpired() throws Exception {
    expectUnauthorized(createExpiredToken(jwtKey));
  }

  private void expectOk(String token) throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/authenticate").header(AUTHORIZATION, BEARER + token)).andExpect(status().isOk());
  }

  private void expectUnauthorized(String token) throws Exception {
    mvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate").header(AUTHORIZATION, BEARER + token))
        .andExpect(status().isUnauthorized());
  }
}
