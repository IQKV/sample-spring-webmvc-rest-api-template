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

package com.iqkv.sample.webmvc.dashboard.security.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.boot.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for SecurityUtils within the Security bounded context.
 * <p>
 * Tests security utility functions for authentication, authorization,
 * and user context management.
 */
class SecurityUtilsTest {

  @BeforeEach
  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldGetCurrentUserLogin() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
    SecurityContextHolder.setContext(securityContext);

    // When
    Optional<String> login = SecurityUtils.getCurrentUserLogin();

    // Then
    assertThat(login).contains("admin");
  }

  @Test
  void shouldReturnEmptyWhenNoAuthentication() {
    // Given - No authentication set

    // When
    Optional<String> login = SecurityUtils.getCurrentUserLogin();

    // Then
    assertThat(login).isEmpty();
  }

  @Test
  void shouldGetCurrentUserJWT() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "jwt-token"));
    SecurityContextHolder.setContext(securityContext);

    // When
    Optional<String> jwt = SecurityUtils.getCurrentUserJWT();

    // Then
    assertThat(jwt).contains("jwt-token");
  }

  @Test
  void shouldDetectAuthenticatedUser() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
    SecurityContextHolder.setContext(securityContext);

    // When
    boolean isAuthenticated = SecurityUtils.isAuthenticated();

    // Then
    assertThat(isAuthenticated).isTrue();
  }

  @Test
  void shouldDetectUnauthenticatedUser() {
    // Given - No authentication

    // When
    boolean isAuthenticated = SecurityUtils.isAuthenticated();

    // Then
    assertThat(isAuthenticated).isFalse();
  }

  @Test
  void shouldNotConsiderAnonymousUserAsAuthenticated() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities));
    SecurityContextHolder.setContext(securityContext);

    // When
    boolean isAuthenticated = SecurityUtils.isAuthenticated();

    // Then
    assertThat(isAuthenticated).isFalse();
  }

  @Test
  void shouldCheckUserAuthority() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("user", "user", authorities));
    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThat(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER)).isTrue();
    assertThat(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)).isFalse();
  }

  @Test
  void shouldCheckAnyOfAuthorities() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("user", "user", authorities));
    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThat(SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)).isTrue();
    assertThat(SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ANONYMOUS, AuthoritiesConstants.ADMIN)).isFalse();
  }

  @Test
  void shouldCheckNoneOfAuthorities() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("user", "user", authorities));
    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThat(SecurityUtils.hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)).isFalse();
    assertThat(SecurityUtils.hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.ANONYMOUS, AuthoritiesConstants.ADMIN)).isTrue();
  }

  @Test
  void shouldHandleMultipleAuthorities() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin", authorities));
    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThat(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER)).isTrue();
    assertThat(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)).isTrue();
    assertThat(SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.USER)).isTrue();
    assertThat(SecurityUtils.hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.ANONYMOUS)).isTrue();
  }

  @Test
  void shouldGetCurrentUserAuthorities() {
    // Given
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
    authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin", authorities));
    SecurityContextHolder.setContext(securityContext);

    // When
    Collection<String> userAuthorities = SecurityUtils.getCurrentUserAuthorities();

    // Then
    assertThat(userAuthorities).containsExactlyInAnyOrder(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN);
  }

  @Test
  void shouldReturnEmptyAuthoritiesWhenNotAuthenticated() {
    // Given - No authentication

    // When
    Collection<String> userAuthorities = SecurityUtils.getCurrentUserAuthorities();

    // Then
    assertThat(userAuthorities).isEmpty();
  }
}
