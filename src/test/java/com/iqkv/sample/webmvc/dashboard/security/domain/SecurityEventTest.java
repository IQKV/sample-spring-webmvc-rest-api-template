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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SecurityEvent domain entity within the Security bounded context.
 * <p>
 * Tests the domain logic, validation rules, and business constraints
 * for security event tracking and monitoring.
 */
class SecurityEventTest {

  private SecurityEvent securityEvent;

  @BeforeEach
  void setUp() {
    securityEvent = new SecurityEvent();
  }

  @Test
  void shouldCreateSecurityEventWithValidData() {
    // Given
    SecurityEventType eventType = SecurityEventType.LOGIN_SUCCESS;
    String login = "testuser";
    String ipAddress = "192.168.1.1";
    String userAgent = "Mozilla/5.0";
    String description = "Successful login";
    LocalDateTime timestamp = LocalDateTime.now();

    // When
    securityEvent.setEventType(eventType);
    securityEvent.setLogin(login);
    securityEvent.setIpAddress(ipAddress);
    securityEvent.setUserAgent(userAgent);
    securityEvent.setDescription(description);
    securityEvent.setOccurredAt(timestamp.atZone(ZoneOffset.UTC).toInstant());

    // Then
    assertThat(securityEvent.getEventType()).isEqualTo(eventType);
    assertThat(securityEvent.getLogin()).isEqualTo(login);
    assertThat(securityEvent.getIpAddress()).isEqualTo(ipAddress);
    assertThat(securityEvent.getUserAgent()).isEqualTo(userAgent);
    assertThat(securityEvent.getDescription()).isEqualTo(description);
    assertThat(securityEvent.getTimestamp()).isEqualTo(timestamp);
  }

  @Test
  void shouldValidateRequiredFields() {
    // When & Then
    assertThatThrownBy(() -> securityEvent.setEventType(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event type cannot be null");

    assertThatThrownBy(() -> securityEvent.setOccurredAt(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Occurred time cannot be null");

    assertThatThrownBy(() -> securityEvent.setDescription(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Description cannot be null or empty");

    assertThatThrownBy(() -> securityEvent.setDescription(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Description cannot be null or empty");
  }

  @Test
  void shouldAllowOptionalFields() {
    // Given
    SecurityEventType eventType = SecurityEventType.SYSTEM_EVENT;
    String description = "System maintenance";
    LocalDateTime timestamp = LocalDateTime.now();

    // When
    securityEvent.setEventType(eventType);
    securityEvent.setDescription(description);
    securityEvent.setOccurredAt(timestamp.atZone(ZoneOffset.UTC).toInstant());
    // login, ipAddress, userAgent are optional

    // Then
    assertThat(securityEvent.getEventType()).isEqualTo(eventType);
    assertThat(securityEvent.getDescription()).isEqualTo(description);
    assertThat(securityEvent.getTimestamp()).isEqualTo(timestamp);
    assertThat(securityEvent.getLogin()).isNull();
    assertThat(securityEvent.getIpAddress()).isNull();
    assertThat(securityEvent.getUserAgent()).isNull();
  }

  @Test
  void shouldValidateIpAddressFormat() {
    // When & Then - Valid IP addresses
    securityEvent.setIpAddress("192.168.1.1");
    assertThat(securityEvent.getIpAddress()).isEqualTo("192.168.1.1");

    securityEvent.setIpAddress("10.0.0.1");
    assertThat(securityEvent.getIpAddress()).isEqualTo("10.0.0.1");

    securityEvent.setIpAddress("127.0.0.1");
    assertThat(securityEvent.getIpAddress()).isEqualTo("127.0.0.1");

    // IPv6
    securityEvent.setIpAddress("::1");
    assertThat(securityEvent.getIpAddress()).isEqualTo("::1");
  }

  @Test
  void shouldRejectInvalidIpAddressFormat() {
    // When & Then
    assertThatThrownBy(() -> securityEvent.setIpAddress("invalid-ip"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid IP address format");

    assertThatThrownBy(() -> securityEvent.setIpAddress("999.999.999.999"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid IP address format");

    assertThatThrownBy(() -> securityEvent.setIpAddress("192.168.1"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid IP address format");
  }

  @Test
  void shouldCheckIfEventIsRecent() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime recentTime = now.minusMinutes(5);
    LocalDateTime oldTime = now.minusHours(2);

    // When & Then
    securityEvent.setOccurredAt(recentTime.atZone(ZoneOffset.UTC).toInstant());
    assertThat(securityEvent.isRecent()).isTrue();

    securityEvent.setOccurredAt(oldTime.atZone(ZoneOffset.UTC).toInstant());
    assertThat(securityEvent.isRecent()).isFalse();
  }

  @Test
  void shouldCheckIfEventIsSuspicious() {
    // When & Then
    securityEvent.setEventType(SecurityEventType.LOGIN_FAILURE);
    assertThat(securityEvent.isSuspicious()).isTrue();

    securityEvent.setEventType(SecurityEventType.SUSPICIOUS_ACTIVITY);
    assertThat(securityEvent.isSuspicious()).isTrue();

    securityEvent.setEventType(SecurityEventType.IP_BLOCKED);
    assertThat(securityEvent.isSuspicious()).isTrue();

    securityEvent.setEventType(SecurityEventType.LOGIN_SUCCESS);
    assertThat(securityEvent.isSuspicious()).isFalse();

    securityEvent.setEventType(SecurityEventType.USER_REGISTERED);
    assertThat(securityEvent.isSuspicious()).isFalse();
  }

  @Test
  void shouldCheckIfEventRequiresUserContext() {
    // When & Then
    securityEvent.setEventType(SecurityEventType.LOGIN_SUCCESS);
    assertThat(securityEvent.requiresUserContext()).isTrue();

    securityEvent.setEventType(SecurityEventType.LOGIN_FAILURE);
    assertThat(securityEvent.requiresUserContext()).isTrue();

    securityEvent.setEventType(SecurityEventType.USER_REGISTERED);
    assertThat(securityEvent.requiresUserContext()).isTrue();

    securityEvent.setEventType(SecurityEventType.SYSTEM_EVENT);
    assertThat(securityEvent.requiresUserContext()).isFalse();

    securityEvent.setEventType(SecurityEventType.IP_BLOCKED);
    assertThat(securityEvent.requiresUserContext()).isFalse();
  }

  @Test
  void shouldGetEventSeverity() {
    // When & Then
    securityEvent.setEventType(SecurityEventType.LOGIN_SUCCESS);
    assertThat(securityEvent.getSeverity()).isEqualTo(SecurityEventSeverity.INFO);

    securityEvent.setEventType(SecurityEventType.LOGIN_FAILURE);
    assertThat(securityEvent.getSeverity()).isEqualTo(SecurityEventSeverity.WARNING);

    securityEvent.setEventType(SecurityEventType.SUSPICIOUS_ACTIVITY);
    assertThat(securityEvent.getSeverity()).isEqualTo(SecurityEventSeverity.HIGH);

    securityEvent.setEventType(SecurityEventType.IP_BLOCKED);
    assertThat(securityEvent.getSeverity()).isEqualTo(SecurityEventSeverity.CRITICAL);
  }

  @Test
  void shouldStoreAndRetrieveMetadata() {
    // Given
    Map<String, Object> metadata = Map.of(
        "attemptCount", 3,
        "reason", "Invalid credentials",
        "blocked", true
    );

    // When
    securityEvent.setMetadata(metadata);

    // Then
    assertThat(securityEvent.getMetadata()).isEqualTo(metadata);
    assertThat(securityEvent.getMetadataValue("attemptCount")).isEqualTo(3);
    assertThat(securityEvent.getMetadataValue("reason")).isEqualTo("Invalid credentials");
    assertThat(securityEvent.getMetadataValue("blocked")).isEqualTo(true);
    assertThat(securityEvent.getMetadataValue("nonexistent")).isNull();
  }

  @Test
  void shouldHandleNullMetadata() {
    // When
    securityEvent.setMetadata(null);

    // Then
    assertThat(securityEvent.getMetadata()).isNull();
    assertThat(securityEvent.getMetadataValue("anyKey")).isNull();
  }

  @Test
  void shouldFormatEventSummary() {
    // Given
    securityEvent.setEventType(SecurityEventType.LOGIN_FAILURE);
    securityEvent.setLogin("testuser");
    securityEvent.setIpAddress("192.168.1.1");
    securityEvent.setOccurredAt(LocalDateTime.of(2025, 1, 6, 10, 30).atZone(ZoneOffset.UTC).toInstant());

    // When
    String summary = securityEvent.getSummary();

    // Then
    assertThat(summary).isEqualTo("LOGIN_FAILURE for testuser from 192.168.1.1 at 2025-01-06T10:30");
  }

  @Test
  void shouldFormatEventSummaryWithoutUser() {
    // Given
    securityEvent.setEventType(SecurityEventType.SYSTEM_EVENT);
    securityEvent.setOccurredAt(LocalDateTime.of(2025, 1, 6, 10, 30).atZone(ZoneOffset.UTC).toInstant());

    // When
    String summary = securityEvent.getSummary();

    // Then
    assertThat(summary).isEqualTo("SYSTEM_EVENT at 2025-01-06T10:30");
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    SecurityEvent event1 = new SecurityEvent();
    event1.setId(1L);
    event1.setEventType(SecurityEventType.LOGIN_SUCCESS);
    event1.setLogin("testuser");

    SecurityEvent event2 = new SecurityEvent();
    event2.setId(1L);
    event2.setEventType(SecurityEventType.LOGIN_SUCCESS);
    event2.setLogin("testuser");

    SecurityEvent event3 = new SecurityEvent();
    event3.setId(2L);
    event3.setEventType(SecurityEventType.LOGIN_FAILURE);
    event3.setLogin("otheruser");

    // When & Then
    assertThat(event1).isEqualTo(event2);
    assertThat(event1).isNotEqualTo(event3);
    assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    securityEvent.setId(1L);
    securityEvent.setEventType(SecurityEventType.LOGIN_SUCCESS);
    securityEvent.setLogin("testuser");
    securityEvent.setIpAddress("192.168.1.1");

    // When
    String result = securityEvent.toString();

    // Then
    assertThat(result).contains("SecurityEvent{");
    assertThat(result).contains("id=1");
    assertThat(result).contains("eventType=LOGIN_SUCCESS");
    assertThat(result).contains("login='testuser'");
    assertThat(result).contains("ipAddress='192.168.1.1'");
  }

  @Test
  void shouldCreateEventWithBuilder() {
    // Given
    SecurityEventType eventType = SecurityEventType.LOGIN_SUCCESS;
    String login = "testuser";
    String ipAddress = "192.168.1.1";
    String description = "Successful login";
    LocalDateTime timestamp = LocalDateTime.now();

    // When
    SecurityEvent event = SecurityEvent.builder()
        .eventType(eventType)
        .login(login)
        .ipAddress(ipAddress)
        .description(description)
        .timestamp(timestamp)
        .build();

    // Then
    assertThat(event.getEventType()).isEqualTo(eventType);
    assertThat(event.getLogin()).isEqualTo(login);
    assertThat(event.getIpAddress()).isEqualTo(ipAddress);
    assertThat(event.getDescription()).isEqualTo(description);
    assertThat(event.getTimestamp()).isEqualTo(timestamp);
  }
}
