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

package com.iqkv.sample.webmvc.dashboard.security.infrastructure;

import static com.iqkv.sample.webmvc.dashboard.security.jwt.JwtAuthenticationTestUtils.BEARER;
import static com.iqkv.sample.webmvc.dashboard.security.jwt.JwtAuthenticationTestUtils.createExpiredToken;
import static com.iqkv.sample.webmvc.dashboard.security.jwt.JwtAuthenticationTestUtils.createSignedInvalidJwt;
import static com.iqkv.sample.webmvc.dashboard.security.jwt.JwtAuthenticationTestUtils.createTokenWithDifferentSignature;
import static com.iqkv.sample.webmvc.dashboard.security.jwt.JwtAuthenticationTestUtils.createValidToken;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.BoundedContextTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Integration tests for JWT Token Authentication within the Security bounded context.
 * <p>
 * Tests the infrastructure layer's JWT token validation, parsing, and security filter chain
 * integration for authentication and authorization scenarios.
 */
@AutoConfigureMockMvc
@IntegrationTest
class TokenAuthenticationIT extends BoundedContextTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Value("${application-configuration.security.authentication.jwt.base64-secret}")
  private String jwtSecret;

  @Test
  void shouldAcceptValidJwtToken() throws Exception {
    // Given
    String validToken = createValidToken(jwtSecret);

    // When & Then
    expectAuthenticated(validToken);
  }

  @Test
  void shouldRejectTokenWithInvalidSignature() throws Exception {
    // Given
    String tokenWithInvalidSignature = createTokenWithDifferentSignature();

    // When & Then
    expectUnauthorized(tokenWithInvalidSignature);
  }

  @Test
  void shouldRejectMalformedJwtToken() throws Exception {
    // Given
    String malformedToken = createSignedInvalidJwt(jwtSecret);

    // When & Then
    expectUnauthorized(malformedToken);
  }

  @Test
  void shouldRejectExpiredJwtToken() throws Exception {
    // Given
    String expiredToken = createExpiredToken(jwtSecret);

    // When & Then
    expectUnauthorized(expiredToken);
  }

  @Test
  void shouldRejectEmptyToken() throws Exception {
    // Given
    String emptyToken = "";

    // When & Then
    expectUnauthorized(emptyToken);
  }

  @Test
  void shouldRejectNullToken() throws Exception {
    // When & Then
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRejectTokenWithoutBearerPrefix() throws Exception {
    // Given
    String validTokenWithoutBearer = createValidToken(jwtSecret);

    // When & Then
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate")
            .header(AUTHORIZATION, validTokenWithoutBearer)) // Missing "Bearer " prefix
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRejectTokenWithInvalidBearerFormat() throws Exception {
    // Given
    String validToken = createValidToken(jwtSecret);
    String invalidBearerFormat = "InvalidPrefix " + validToken;

    // When & Then
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate")
            .header(AUTHORIZATION, invalidBearerFormat))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldHandleTokenWithExtraSpaces() throws Exception {
    // Given
    String validToken = createValidToken(jwtSecret);
    String tokenWithSpaces = "Bearer  " + validToken + "  "; // Extra spaces

    // When & Then
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate")
            .header(AUTHORIZATION, tokenWithSpaces))
        .andExpect(status().isUnauthorized()); // Should be rejected due to format
  }

  @Test
  void shouldValidateTokenClaims() throws Exception {
    // Given
    String validToken = createValidToken(jwtSecret);

    // When & Then - Valid token should allow access to protected endpoint
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/account")
            .header(AUTHORIZATION, BEARER + validToken))
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectAccessToProtectedEndpointWithoutToken() throws Exception {
    // When & Then
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/account"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRejectAccessToProtectedEndpointWithInvalidToken() throws Exception {
    // Given
    String invalidToken = createTokenWithDifferentSignature();

    // When & Then
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/account")
            .header(AUTHORIZATION, BEARER + invalidToken))
        .andExpect(status().isUnauthorized());
  }

  @Override
  protected void cleanupBoundedContextData() {
    // No specific cleanup needed for token authentication tests
    // JWT tokens are stateless and don't require database cleanup
  }

  private void expectAuthenticated(String token) throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate")
            .header(AUTHORIZATION, BEARER + token))
        .andExpect(status().isOk());
  }

  private void expectUnauthorized(String token) throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/authenticate")
            .header(AUTHORIZATION, BEARER + token))
        .andExpect(status().isUnauthorized());
  }
}
