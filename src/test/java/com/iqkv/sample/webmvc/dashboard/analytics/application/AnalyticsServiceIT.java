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

package com.iqkv.sample.webmvc.dashboard.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.data.domain.Pageable;
import java.util.Map;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.infrastructure.UserActivityRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AnalyticsService within the Analytics bounded context.
 * <p>
 * Tests the application layer's analytics workflows, event processing,
 * and cross-bounded context integration for user activity tracking.
 */
@SpringBootTest
@IntegrationTest
class AnalyticsServiceIT {

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private UserActivityRepository userActivityRepository;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @AfterEach
  void tearDown() {
    userActivityRepository.deleteAll();
  }

  @Test
  @Transactional
  void shouldTrackUserRegistrationActivity() {
    // Given
    UserRegisteredEvent event = new UserRegisteredEvent(
        1L,
        "testuser",
        "test@example.com",
        "Test",
        "User"
    );

    // When
    eventPublisher.publishEvent(event);

    // Then
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      List<UserActivity> activities = userActivityRepository.findByUserId(1L);
      assertThat(activities).hasSize(1);

      UserActivity activity = activities.get(0);
      assertThat(activity.getActivityType()).isEqualTo(ActivityType.USER_REGISTERED);
      assertThat(activity.getUserId()).isEqualTo(1L);
      assertThat(activity.getMetadata()).containsEntry("login", "testuser");
      assertThat(activity.getMetadata()).containsEntry("email", "test@example.com");
    });
  }

  @Test
  @Transactional
  void shouldTrackUserActivationActivity() {
    // Given
    UserActivatedEvent event = new UserActivatedEvent(
        1L,
        "testuser",
        "test@example.com"
    );

    // When
    eventPublisher.publishEvent(event);

    // Then
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      List<UserActivity> activities = userActivityRepository.findByUserId(1L);
      assertThat(activities).hasSize(1);

      UserActivity activity = activities.get(0);
      assertThat(activity.getActivityType()).isEqualTo(ActivityType.USER_ACTIVATED);
      assertThat(activity.getUserId()).isEqualTo(1L);
      assertThat(activity.getMetadata()).containsEntry("login", "testuser");
      assertThat(activity.getMetadata()).containsEntry("email", "test@example.com");
    });
  }

  @Test
  @Transactional
  void shouldRecordUserActivity() {
    // Given
    Long userId = 1L;
    ActivityType activityType = ActivityType.LOGIN;
    Map<String, Object> metadata = Map.of(
        "ipAddress", "192.168.1.1",
        "userAgent", "Mozilla/5.0"
    );

    // When
    analyticsService.recordUserActivity(userId, activityType, metadata);

    // Then
    List<UserActivity> activities = userActivityRepository.findByUserId(userId);
    assertThat(activities).hasSize(1);

    UserActivity activity = activities.get(0);
    assertThat(activity.getActivityType()).isEqualTo(ActivityType.LOGIN);
    assertThat(activity.getUserId()).isEqualTo(userId);
    assertThat(activity.getMetadata()).containsEntry("ipAddress", "192.168.1.1");
    assertThat(activity.getMetadata()).containsEntry("userAgent", "Mozilla/5.0");
    assertThat(activity.getTimestamp()).isNotNull();
  }

  @Test
  @Transactional
  void shouldGetUserActivitiesByDateRange() {
    // Given
    Long userId = 1L;
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime yesterday = now.minusDays(1);
    LocalDateTime tomorrow = now.plusDays(1);

    // Create activities at different times
    UserActivity activity1 = createUserActivity(userId, ActivityType.LOGIN, yesterday);
    UserActivity activity2 = createUserActivity(userId, ActivityType.LOGOUT, now);
    UserActivity activity3 = createUserActivity(userId, ActivityType.LOGIN, tomorrow);

    userActivityRepository.saveAll(List.of(activity1, activity2, activity3));

    // When
    List<UserActivity> activities = analyticsService.getUserActivities(
        userId,
        yesterday.toLocalDate(),
        now.toLocalDate()
    );

    // Then
    assertThat(activities).hasSize(2);
    assertThat(activities).extracting(UserActivity::getActivityType)
        .containsExactly(ActivityType.LOGIN, ActivityType.LOGOUT);
  }

  @Test
  @Transactional
  void shouldGetDailyActivityCounts() {
    // Given
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    // Create activities for different days
    createAndSaveActivity(1L, ActivityType.LOGIN, today.atStartOfDay());
    createAndSaveActivity(1L, ActivityType.LOGOUT, today.atTime(12, 0));
    createAndSaveActivity(2L, ActivityType.LOGIN, today.atTime(14, 0));
    createAndSaveActivity(1L, ActivityType.LOGIN, yesterday.atStartOfDay());

    // When
    Map<LocalDate, Long> dailyCounts = analyticsService.getDailyActivityCounts(
        yesterday,
        today
    );

    // Then
    assertThat(dailyCounts).hasSize(2);
    assertThat(dailyCounts.get(today)).isEqualTo(3L);
    assertThat(dailyCounts.get(yesterday)).isEqualTo(1L);
  }

  @Test
  @Transactional
  void shouldGetActivityCountsByType() {
    // Given
    LocalDate today = LocalDate.now();

    // Create activities of different types
    createAndSaveActivity(1L, ActivityType.LOGIN, today.atStartOfDay());
    createAndSaveActivity(1L, ActivityType.LOGIN, today.atTime(12, 0));
    createAndSaveActivity(2L, ActivityType.LOGOUT, today.atTime(14, 0));
    createAndSaveActivity(3L, ActivityType.USER_REGISTERED, today.atTime(16, 0));

    // When
    Map<ActivityType, Long> typeCounts = analyticsService.getActivityCountsByType(
        today,
        today
    );

    // Then
    assertThat(typeCounts).hasSize(3);
    assertThat(typeCounts.get(ActivityType.LOGIN)).isEqualTo(2L);
    assertThat(typeCounts.get(ActivityType.LOGOUT)).isEqualTo(1L);
    assertThat(typeCounts.get(ActivityType.USER_REGISTERED)).isEqualTo(1L);
  }

  @Test
  @Transactional
  void shouldGetMostActiveUsers() {
    // Given
    LocalDate today = LocalDate.now();

    // Create activities for different users
    createAndSaveActivity(1L, ActivityType.LOGIN, today.atStartOfDay());
    createAndSaveActivity(1L, ActivityType.LOGOUT, today.atTime(12, 0));
    createAndSaveActivity(1L, ActivityType.LOGIN, today.atTime(14, 0));
    createAndSaveActivity(2L, ActivityType.LOGIN, today.atTime(16, 0));
    createAndSaveActivity(2L, ActivityType.LOGOUT, today.atTime(18, 0));
    createAndSaveActivity(3L, ActivityType.LOGIN, today.atTime(20, 0));

    // When
    List<Map<String, Object>> mostActiveUsers = analyticsService.getMostActiveUsers(
        today,
        today,
        2
    );

    // Then
    assertThat(mostActiveUsers).hasSize(2);

    Map<String, Object> firstUser = mostActiveUsers.get(0);
    assertThat(firstUser.get("userId")).isEqualTo(1L);
    assertThat(firstUser.get("activityCount")).isEqualTo(3L);

    Map<String, Object> secondUser = mostActiveUsers.get(1);
    assertThat(secondUser.get("userId")).isEqualTo(2L);
    assertThat(secondUser.get("activityCount")).isEqualTo(2L);
  }

  @Test
  @Transactional
  void shouldCleanupOldActivities() {
    // Given
    LocalDateTime oldDate = LocalDateTime.now().minusDays(91); // Older than retention period
    LocalDateTime recentDate = LocalDateTime.now().minusDays(30);

    UserActivity oldActivity = createUserActivity(1L, ActivityType.LOGIN, oldDate);
    UserActivity recentActivity = createUserActivity(1L, ActivityType.LOGIN, recentDate);

    userActivityRepository.saveAll(List.of(oldActivity, recentActivity));

    // When
    analyticsService.cleanupOldActivities();

    // Then
    assertThat(userActivityRepository.findById(oldActivity.getId())).isEmpty();
    assertThat(userActivityRepository.findById(recentActivity.getId())).isPresent();
  }

  private UserActivity createUserActivity(Long userId, ActivityType activityType, LocalDateTime timestamp) {
    UserActivity activity = new UserActivity();
    activity.setUserId(userId);
    activity.setActivityType(activityType);
    activity.setOccurredAt(timestamp.atZone(ZoneOffset.UTC).toInstant());
    activity.setMetadata(Map.of("test", "data"));
    return activity;
  }

  private void createAndSaveActivity(Long userId, ActivityType activityType, LocalDateTime timestamp) {
    UserActivity activity = createUserActivity(userId, activityType, timestamp);
    userActivityRepository.save(activity);
  }
}
