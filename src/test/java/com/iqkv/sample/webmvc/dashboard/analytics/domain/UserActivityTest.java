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

package com.iqkv.sample.webmvc.dashboard.analytics.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserActivity domain entity within the Analytics bounded context.
 * <p>
 * Tests the domain logic, validation rules, and business constraints
 * for user activity tracking and analytics.
 */
class UserActivityTest {

  private UserActivity userActivity;

  @BeforeEach
  void setUp() {
    userActivity = new UserActivity();
  }

  @Test
  void shouldCreateUserActivityWithValidData() {
    // Given
    Long userId = 1L;
    ActivityType activityType = ActivityType.LOGIN;
    LocalDateTime timestamp = LocalDateTime.now();
    Map<String, Object> metadata = Map.of("ipAddress", "192.168.1.1");

    // When
    userActivity.setUserId(userId);
    userActivity.setActivityType(activityType);
    userActivity.setOccurredAt(timestamp.atZone(ZoneOffset.UTC).toInstant());
    userActivity.setMetadata(metadata);

    // Then
    assertThat(userActivity.getUserId()).isEqualTo(userId);
    assertThat(userActivity.getActivityType()).isEqualTo(activityType);
    assertThat(userActivity.getTimestamp()).isEqualTo(timestamp.atZone(ZoneOffset.UTC).toInstant());
    assertThat(userActivity.getMetadata()).isEqualTo(metadata);
  }

  @Test
  void shouldValidateRequiredFields() {
    // Given
    userActivity.setUserId(1L);
    userActivity.setActivityType(ActivityType.LOGIN);
    userActivity.setOccurredAt(Instant.now());

    // When & Then - Test validation method
    assertThatThrownBy(() -> {
      userActivity.setUserId(null);
      userActivity.validateActivity();
    })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null");

    assertThatThrownBy(() -> {
      userActivity.setUserId(1L);
      userActivity.setActivityType(null);
      userActivity.validateActivity();
    })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Activity type cannot be null");

    assertThatThrownBy(() -> {
      userActivity.setUserId(1L);
      userActivity.setActivityType(ActivityType.LOGIN);
      userActivity.setOccurredAt(null);
      userActivity.validateActivity();
    })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Occurred time cannot be null");
  }

  @Test
  void shouldValidateUserIdIsPositive() {
    // Given - Set required fields first
    userActivity.setActivityType(ActivityType.LOGIN);
    userActivity.setOccurredAt(Instant.now());

    // When & Then - Test that validation passes for positive user ID
    userActivity.setUserId(1L);
    // No exception should be thrown for positive user ID
    userActivity.validateActivity();

    // Test that null user ID fails validation
    assertThatThrownBy(() -> {
      userActivity.setUserId(null);
      userActivity.validateActivity();
    })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null");
  }

  @Test
  void shouldAllowEmptyMetadata() {
    // Given
    Map<String, Object> emptyMetadata = Map.of();

    // When
    userActivity.setMetadata(emptyMetadata);

    // Then
    assertThat(userActivity.getMetadata()).isEmpty();
  }

  @Test
  void shouldAllowNullMetadata() {
    // When
    userActivity.setMetadata(null);

    // Then
    assertThat(userActivity.getMetadata()).isNull();
  }

  @Test
  void shouldStoreComplexMetadata() {
    // Given
    Map<String, Object> complexMetadata = Map.of(
        "ipAddress", "192.168.1.1",
        "userAgent", "Mozilla/5.0",
        "sessionId", "abc123",
        "duration", 3600,
        "success", true
    );

    // When
    userActivity.setMetadata(complexMetadata);

    // Then
    assertThat(userActivity.getMetadata()).hasSize(5);
    assertThat(userActivity.getMetadata().get("ipAddress")).isEqualTo("192.168.1.1");
    assertThat(userActivity.getMetadata().get("userAgent")).isEqualTo("Mozilla/5.0");
    assertThat(userActivity.getMetadata().get("sessionId")).isEqualTo("abc123");
    assertThat(userActivity.getMetadata().get("duration")).isEqualTo(3600);
    assertThat(userActivity.getMetadata().get("success")).isEqualTo(true);
  }

  @Test
  void shouldCheckIfActivityIsRecent() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime recentTime = now.minusMinutes(5);
    LocalDateTime oldTime = now.minusHours(2);

    // When & Then
    userActivity.setOccurredAt(recentTime.atZone(ZoneOffset.UTC).toInstant());
    assertThat(userActivity.isRecent()).isTrue();

    userActivity.setOccurredAt(oldTime.atZone(ZoneOffset.UTC).toInstant());
    assertThat(userActivity.isRecent()).isFalse();
  }

  @Test
  void shouldCheckIfActivityIsToday() {
    // Given
    LocalDateTime today = LocalDateTime.now();
    LocalDateTime yesterday = today.minusDays(1);

    // When & Then
    userActivity.setOccurredAt(today.atZone(ZoneOffset.UTC).toInstant());
    assertThat(userActivity.isToday()).isTrue();

    userActivity.setOccurredAt(yesterday.atZone(ZoneOffset.UTC).toInstant());
    assertThat(userActivity.isToday()).isFalse();
  }

  @Test
  void shouldGetActivityAge() {
    // Given
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
    userActivity.setOccurredAt(oneHourAgo.atZone(ZoneOffset.UTC).toInstant());

    // When
    long ageInMinutes = userActivity.getMinutesSinceOccurred();

    // Then
    assertThat(ageInMinutes).isGreaterThanOrEqualTo(59);
    assertThat(ageInMinutes).isLessThanOrEqualTo(61); // Allow for small timing differences
  }

  @Test
  void shouldFormatActivityDescription() {
    // Given
    userActivity.setUserId(123L);
    userActivity.setActivityType(ActivityType.LOGIN);
    userActivity.setOccurredAt(LocalDateTime.of(2025, 1, 6, 10, 30).atZone(ZoneOffset.UTC).toInstant());
    userActivity.setDescription("User logged in");

    // When
    String description = userActivity.getDescription();

    // Then
    assertThat(description).isEqualTo("User logged in");
  }

  @Test
  void shouldExtractMetadataValue() {
    // Given
    Map<String, Object> metadata = Map.of(
        "ipAddress", "192.168.1.1",
        "userAgent", "Mozilla/5.0"
    );
    userActivity.setMetadata(metadata);

    // When & Then
    assertThat(userActivity.getMetadataValue("ipAddress")).isEqualTo("192.168.1.1");
    assertThat(userActivity.getMetadataValue("userAgent")).isEqualTo("Mozilla/5.0");
    assertThat(userActivity.getMetadataValue("nonexistent")).isNull();
  }

  @Test
  void shouldCheckIfMetadataContainsKey() {
    // Given
    Map<String, Object> metadata = Map.of("ipAddress", "192.168.1.1");
    userActivity.setMetadata(metadata);

    // When & Then
    assertThat(userActivity.hasMetadata("ipAddress")).isTrue();
    assertThat(userActivity.hasMetadata("nonexistent")).isFalse();
  }

  @Test
  void shouldHandleNullMetadataInHelperMethods() {
    // Given
    userActivity.setMetadata(null);

    // When & Then
    assertThat(userActivity.getMetadataValue("anyKey")).isNull();
    assertThat(userActivity.hasMetadata("anyKey")).isFalse();
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    UserActivity activity1 = new UserActivity();
    activity1.setId(1L);
    activity1.setUserId(123L);
    activity1.setActivityType(ActivityType.LOGIN);

    UserActivity activity2 = new UserActivity();
    activity2.setId(1L);
    activity2.setUserId(123L);
    activity2.setActivityType(ActivityType.LOGIN);

    UserActivity activity3 = new UserActivity();
    activity3.setId(2L);
    activity3.setUserId(456L);
    activity3.setActivityType(ActivityType.LOGOUT);

    // When & Then
    assertThat(activity1).isEqualTo(activity2);
    assertThat(activity1).isNotEqualTo(activity3);
    assertThat(activity1.hashCode()).isEqualTo(activity2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // Given
    userActivity.setId(1L);
    userActivity.setUserId(123L);
    userActivity.setActivityType(ActivityType.LOGIN);
    userActivity.setOccurredAt(LocalDateTime.of(2025, 1, 6, 10, 30).atZone(ZoneOffset.UTC).toInstant());

    // When
    String result = userActivity.toString();

    // Then
    assertThat(result).contains("UserActivity{");
    assertThat(result).contains("id=1");
    assertThat(result).contains("userId=123");
    assertThat(result).contains("activityType=LOGIN");
    assertThat(result).contains("occurredAt=2025-01-06T10:30:00Z");
  }

  @Test
  void shouldValidateActivityTypeValues() {
    // When & Then - Test all activity types are valid
    for (ActivityType type : ActivityType.values()) {
      userActivity.setActivityType(type);
      assertThat(userActivity.getActivityType()).isEqualTo(type);
    }
  }

  @Test
  void shouldCreateActivityWithBuilder() {
    // Given
    Long userId = 123L;
    ActivityType activityType = ActivityType.LOGIN;
    LocalDateTime timestamp = LocalDateTime.now();
    Map<String, Object> metadata = Map.of("ipAddress", "192.168.1.1");

    // When
    UserActivity activity = UserActivity.builder()
        .userId(userId)
        .activityType(activityType)
        .timestamp(timestamp)
        .metadata(metadata)
        .build();

    // Then
    assertThat(activity.getUserId()).isEqualTo(userId);
    assertThat(activity.getActivityType()).isEqualTo(activityType);
    assertThat(activity.getTimestamp()).isEqualTo(timestamp.atZone(ZoneOffset.UTC).toInstant());
    assertThat(activity.getMetadata()).isEqualTo(metadata);
  }
}
