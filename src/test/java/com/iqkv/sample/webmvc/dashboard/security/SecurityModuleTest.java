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

package com.iqkv.sample.webmvc.dashboard.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import com.iqkv.sample.webmvc.dashboard.security.application.SecurityFacade;
import com.iqkv.sample.webmvc.dashboard.security.application.SecurityMetricsService;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEvent;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventDomainRepository;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventType;
import com.iqkv.sample.webmvc.dashboard.security.domain.ThreatLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Modulith integration test for Security bounded context.
 * <p>
 * This test validates that the Security module works correctly in isolation
 * and properly handles security events and monitoring.
 */
@ApplicationModuleTest
@ActiveProfiles("test")
class SecurityModuleTest {

  @Autowired
  private SecurityFacade securityFacade;

  @Autowired
  private SecurityMetricsService securityMetricsService;

  @Autowired
  private SecurityEventDomainRepository securityEventRepository;

  @Test
  void shouldCreateFailedLoginSecurityEvent() {
    // Given
    String login = "securitytest";
    String ipAddress = "192.168.1.200";
    String userAgent = "Security Test Agent";

    // When
    SecurityEvent event = SecurityEvent.failedLogin(login, ipAddress, userAgent);
    SecurityEvent saved = securityEventRepository.save(event);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEventType()).isEqualTo(SecurityEventType.FAILED_LOGIN);
    assertThat(saved.getThreatLevel()).isEqualTo(ThreatLevel.LOW);
    assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
    assertThat(saved.getUserAgent()).isEqualTo(userAgent);
    assertThat(saved.getDescription()).contains(login);
    assertThat(saved.isResolved()).isFalse();
  }

  @Test
  void shouldCreateSuspiciousActivityEvent() {
    // Given
    Long userId = 1000L;
    String sessionId = "session-suspicious";
    String description = "Multiple failed login attempts detected";
    Map<String, Object> data = Map.of(
        "attempts", 5,
        "timeWindow", "5 minutes",
        "ipAddress", "10.0.0.100"
    );

    // When
    SecurityEvent event = SecurityEvent.suspiciousActivity(userId, sessionId, description, data);
    SecurityEvent saved = securityEventRepository.save(event);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEventType()).isEqualTo(SecurityEventType.SUSPICIOUS_ACTIVITY);
    assertThat(saved.getThreatLevel()).isEqualTo(ThreatLevel.MEDIUM);
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getSessionId()).isEqualTo(sessionId);
    assertThat(saved.getDescription()).isEqualTo(description);
    assertThat(saved.getEventData()).isEqualTo(data);
    assertThat(saved.requiresImmediateAttention()).isFalse(); // Medium level
  }

  @Test
  void shouldCreatePrivilegeEscalationEvent() {
    // Given
    Long userId = 1001L;
    String sessionId = "session-privilege";
    String details = "User attempted to access admin functionality";

    // When
    SecurityEvent event = SecurityEvent.privilegeEscalation(userId, sessionId, details);
    SecurityEvent saved = securityEventRepository.save(event);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEventType()).isEqualTo(SecurityEventType.PRIVILEGE_ESCALATION);
    assertThat(saved.getThreatLevel()).isEqualTo(ThreatLevel.HIGH);
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getSessionId()).isEqualTo(sessionId);
    assertThat(saved.getDescription()).contains(details);
    assertThat(saved.requiresImmediateAttention()).isTrue(); // High level
  }

  @Test
  void shouldResolveSecurityEvent() {
    // Given
    SecurityEvent event = SecurityEvent.failedLogin("resolvetest", "192.168.1.201", "Test Agent");
    SecurityEvent saved = securityEventRepository.save(event);
    assertThat(saved.isResolved()).isFalse();

    // When
    saved.resolve("security-admin");

    // Then
    assertThat(saved.isResolved()).isTrue();
    assertThat(saved.getResolvedBy()).isEqualTo("security-admin");
    assertThat(saved.getResolvedAt()).isNotNull();
  }

  @Test
  void shouldEscalateThreatLevel() {
    // Given
    SecurityEvent event = SecurityEvent.failedLogin("escalatetest", "192.168.1.202", "Test Agent");
    SecurityEvent saved = securityEventRepository.save(event);
    assertThat(saved.getThreatLevel()).isEqualTo(ThreatLevel.LOW);

    // When
    saved.escalateThreatLevel(ThreatLevel.HIGH);

    // Then
    assertThat(saved.getThreatLevel()).isEqualTo(ThreatLevel.HIGH);
    assertThat(saved.requiresImmediateAttention()).isTrue();
  }

  @Test
  void shouldValidateSecurityEventAging() {
    // Given
    SecurityEvent event = SecurityEvent.suspiciousActivity(
        1002L,
        "session-aging",
        "Test aging functionality",
        Map.of("test", "aging")
    );
    SecurityEvent saved = securityEventRepository.save(event);

    // When & Then
    assertThat(saved.getAgeInMinutes()).isGreaterThanOrEqualTo(0);
    assertThat(saved.getOccurredAt()).isNotNull();
    assertThat(saved.getOccurredAt()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void shouldFindUnresolvedEvents() {
    // Given - Create some unresolved events
    SecurityEvent event1 = SecurityEvent.failedLogin("unresolved1", "192.168.1.203", "Agent1");
    SecurityEvent event2 = SecurityEvent.suspiciousActivity(1003L, "session1", "Suspicious", Map.of());

    securityEventRepository.save(event1);
    securityEventRepository.save(event2);

    // When
    Page<SecurityEvent> unresolvedEvents = securityEventRepository.findUnresolvedEvents(
        PageRequest.of(0, 10)
    );

    // Then
    assertThat(unresolvedEvents.getContent()).isNotEmpty();
    assertThat(unresolvedEvents.getContent())
        .allMatch(event -> !event.isResolved());
  }

  @Test
  void shouldFindEventsRequiringImmediateAttention() {
    // Given - Create high-priority events
    SecurityEvent criticalEvent = SecurityEvent.privilegeEscalation(
        1004L, "session-critical", "Critical security breach"
    );
    criticalEvent.escalateThreatLevel(ThreatLevel.CRITICAL);
    securityEventRepository.save(criticalEvent);

    SecurityEvent highEvent = SecurityEvent.privilegeEscalation(
        1005L, "session-high", "High priority event"
    );
    securityEventRepository.save(highEvent);

    // When
    Page<SecurityEvent> urgentEvents = securityEventRepository.findEventsRequiringImmediateAttention(
        PageRequest.of(0, 10)
    );

    // Then
    assertThat(urgentEvents.getContent()).isNotEmpty();
    assertThat(urgentEvents.getContent())
        .allMatch(SecurityEvent::requiresImmediateAttention);
  }

  @Test
  void shouldCountFailedLoginAttempts() {
    // Given
    Long userId = 1006L;
    String ipAddress = "192.168.1.204";
    Instant startTime = Instant.now().minusSeconds(3600); // 1 hour ago
    Instant endTime = Instant.now();

    // Create multiple failed login attempts
    for (int i = 0; i < 3; i++) {
      SecurityEvent event = SecurityEvent.failedLogin("counttest" + i, ipAddress, "Agent");
      event.setUserId(userId);
      securityEventRepository.save(event);
    }

    // When
    long count = securityEventRepository.countFailedLoginAttempts(userId, ipAddress, startTime, endTime);

    // Then
    assertThat(count).isGreaterThanOrEqualTo(3);
  }

  @Test
  void shouldFindEventsByIpAddress() {
    // Given
    String testIpAddress = "192.168.1.205";

    SecurityEvent event1 = SecurityEvent.failedLogin("iptest1", testIpAddress, "Agent1");
    SecurityEvent event2 = SecurityEvent.failedLogin("iptest2", testIpAddress, "Agent2");

    securityEventRepository.save(event1);
    securityEventRepository.save(event2);

    // When
    Page<SecurityEvent> eventsByIp = securityEventRepository.findByIpAddress(
        testIpAddress, PageRequest.of(0, 10)
    );

    // Then
    assertThat(eventsByIp.getContent()).hasSize(2);
    assertThat(eventsByIp.getContent())
        .allMatch(event -> testIpAddress.equals(event.getIpAddress()));
  }

  @Test
  void shouldValidateSecurityFacadeIntegration() {
    // Verify that SecurityFacade is properly integrated
    assertThat(securityFacade).isNotNull();

    // The facade should provide clean interface to security operations
    // (Actual methods depend on SecurityFacade implementation)
  }

  @Test
  void shouldValidateSecurityMetricsService() {
    // Verify that SecurityMetricsService is properly integrated
    assertThat(securityMetricsService).isNotNull();

    // The service should handle security metrics
    // (Actual methods depend on SecurityMetricsService implementation)
  }

  @Test
  void shouldValidateSecurityModuleIsolation() {
    // Verify that Security module operates independently
    assertThat(securityEventRepository).isNotNull();

    // The module should handle its own domain logic
    SecurityEvent testEvent = new SecurityEvent();
    testEvent.setEventType(SecurityEventType.FAILED_LOGIN);
    testEvent.setThreatLevel(ThreatLevel.LOW);
    testEvent.setDescription("Isolation test");
    testEvent.setOccurredAt(Instant.now());

    SecurityEvent saved = securityEventRepository.save(testEvent);
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEventType()).isEqualTo(SecurityEventType.FAILED_LOGIN);
  }

  @Test
  void shouldValidateSecurityEventsByTimeRange() {
    // Given
    Instant startTime = Instant.now().minusSeconds(1800); // 30 minutes ago
    Instant endTime = Instant.now();

    SecurityEvent event = SecurityEvent.failedLogin("timerange", "192.168.1.206", "Agent");
    securityEventRepository.save(event);

    // When
    Page<SecurityEvent> eventsInRange = securityEventRepository.findByOccurredAtBetween(
        startTime, endTime, PageRequest.of(0, 10)
    );

    // Then
    assertThat(eventsInRange.getContent()).isNotEmpty();
    assertThat(eventsInRange.getContent())
        .allMatch(e -> e.getOccurredAt().isAfter(startTime) && e.getOccurredAt().isBefore(endTime));
  }

  @Test
  void shouldValidateSecurityEventsByUser() {
    // Given
    Long testUserId = 1007L;

    SecurityEvent event1 = SecurityEvent.suspiciousActivity(testUserId, "session1", "Test 1", Map.of());
    SecurityEvent event2 = SecurityEvent.suspiciousActivity(testUserId, "session2", "Test 2", Map.of());

    securityEventRepository.save(event1);
    securityEventRepository.save(event2);

    // When
    Page<SecurityEvent> userEvents = securityEventRepository.findByUserId(
        testUserId, PageRequest.of(0, 10)
    );

    // Then
    assertThat(userEvents.getContent()).hasSize(2);
    assertThat(userEvents.getContent())
        .allMatch(event -> testUserId.equals(event.getUserId()));
  }
}
