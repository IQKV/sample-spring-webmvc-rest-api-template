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

package com.iqkv.sample.webmvc.dashboard.security.application;

import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventType;

/**
 * Facade interface for Security bounded context.
 * Provides a clean contract for other bounded contexts to interact with security functionality.
 */
public interface SecurityFacade {

  /**
   * Get the current authenticated user login.
   *
   * @return the current user login if authenticated
   */
  Optional<String> getCurrentUserLogin();

  /**
   * Check if the current user has a specific authority.
   *
   * @param authority the authority to check
   * @return true if user has the authority, false otherwise
   */
  boolean hasAuthority(String authority);

  /**
   * Check if the current user is authenticated.
   *
   * @return true if user is authenticated, false otherwise
   */
  boolean isAuthenticated();

  /**
   * Log a security event.
   *
   * @param eventType the type of security event
   * @param userId    the user ID involved in the event
   * @param details   additional event details
   */
  void logSecurityEvent(SecurityEventType eventType, Long userId, String details);

  /**
   * Check if the current user is an admin.
   *
   * @return true if user is admin, false otherwise
   */
  boolean isCurrentUserAdmin();

  /**
   * Check if the current user can access a specific resource.
   *
   * @param resourceId the resource identifier
   * @param action     the action to perform
   * @return true if access is allowed, false otherwise
   */
  boolean canAccessResource(String resourceId, String action);
}
