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

import java.time.Instant;
import java.util.Optional;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.boot.security.SecurityUtils;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEvent;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventType;
import com.iqkv.sample.webmvc.dashboard.security.domain.ThreatLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SecurityFacade providing anti-corruption layer
 * for other bounded contexts to interact with security functionality.
 */
@Component
@Transactional(readOnly = true)
public class SecurityFacadeImpl implements SecurityFacade {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityFacadeImpl.class);

  @Override
  public Optional<String> getCurrentUserLogin() {
    return SecurityUtils.getCurrentUserLogin();
  }

  @Override
  public boolean hasAuthority(String authority) {
    return SecurityUtils.hasCurrentUserThisAuthority(authority);
  }

  @Override
  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated() &&
           !authentication.getName().equals("anonymousUser");
  }

  @Override
  @Transactional
  public void logSecurityEvent(SecurityEventType eventType, Long userId, String details) {
    LOG.debug("Logging security event: {} for user: {} - {}", eventType, userId, details);

    // Create security event
    SecurityEvent event = new SecurityEvent();
    event.setEventType(eventType);
    event.setUserId(userId);
    event.setDescription(details);
    event.setOccurredAt(Instant.now());
    event.setThreatLevel(determineThreatLevel(eventType));

    // In a real implementation, this would be saved to a repository
    // For now, we just log it
    LOG.info("Security Event: {} - User: {} - Threat Level: {} - Details: {}",
        eventType, userId, event.getThreatLevel(), details);
  }

  @Override
  public boolean isCurrentUserAdmin() {
    return hasAuthority(AuthoritiesConstants.ADMIN);
  }

  @Override
  public boolean canAccessResource(String resourceId, String action) {
    // Basic implementation - in a real system this would check permissions
    if (!isAuthenticated()) {
      return false;
    }

    // Admin can access everything
    if (isCurrentUserAdmin()) {
      return true;
    }

    // For now, authenticated users can read, but only admins can write
    return "read".equals(action);
  }

  private ThreatLevel determineThreatLevel(SecurityEventType eventType) {
    return switch (eventType) {
      case SUCCESSFUL_LOGIN, LOGOUT -> ThreatLevel.LOW;
      case FAILED_LOGIN, PASSWORD_CHANGE -> ThreatLevel.MEDIUM;
      case ACCOUNT_LOCKED, SUSPICIOUS_ACTIVITY -> ThreatLevel.HIGH;
      default -> ThreatLevel.MEDIUM;
    };
  }
}
