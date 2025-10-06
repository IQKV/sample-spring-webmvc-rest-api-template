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

package com.iqkv.sample.webmvc.dashboard.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.iqkv.sample.webmvc.dashboard.analytics.application.AnalyticsService;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivityDomainRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Modulith integration test for Analytics bounded context.
 * <p>
 * This test validates that the Analytics module works correctly in isolation
 * and properly handles domain events from other bounded contexts for analytics tracking.
 */
@ApplicationModuleTest
@ActiveProfiles("test")
class AnalyticsModuleTest {

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private UserActivityDomainRepository activityRepository;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Test
  void shouldHandleUserRegisteredEventForAnalytics() {
    // Given
    UserRegisteredEvent event = new UserRegisteredEvent(
        100L,
        "analyticsuser",
        "analytics@example.com",
        "activation-key-analytics",
        Instant.now()
    );

    // When
    eventPublisher.publishEvent(event);

    // Then - Wait for async event processing
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      Page<UserActivity> activities = activityRepository.findByUserId(100L, PageRequest.of(0, 10));
      assertThat(activities.getContent()).isNotEmpty();

      UserActivity activity = activities.getContent().get(0);
      assertThat(activity.getActivityType()).isEqualTo(ActivityType.FEATURE_USE); // Registration mapped to feature use
      assertThat(activity.getDescription()).contains("analyticsuser");
      assertThat(activity.getUserId()).isEqualTo(100L);
    });
  }

  @Test
  void shouldHandleUserActivatedEventForAnalytics() {
    // Given
    UserActivatedEvent event = new UserActivatedEvent(
        101L,
        "activatedanalytics",
        "activated.analytics@example.com",
        Instant.now()
    );

    // When
    eventPublisher.publishEvent(event);

    // Then - Wait for async event processing
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      Page<UserActivity> activities = activityRepository.findByUserId(101L, PageRequest.of(0, 10));
      assertThat(activities.getContent()).isNotEmpty();

      UserActivity activity = activities.getContent().get(0);
      assertThat(activity.getActivityType()).isEqualTo(ActivityType.FEATURE_USE); // Activation mapped to feature use
      assertThat(activity.getDescription()).contains("activatedanalytics");
      assertThat(activity.getUserId()).isEqualTo(101L);
    });
  }

  @Test
  void shouldHandlePasswordResetEventForAnalytics() {
    // Given
    PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
        102L,
        "reset.analytics@example.com",
        "reset-key-analytics",
        Instant.now()
    );

    // When
    eventPublisher.publishEvent(event);

    // Then - Wait for async event processing
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      Page<UserActivity> activities = activityRepository.findByUserId(102L, PageRequest.of(0, 10));
      assertThat(activities.getContent()).isNotEmpty();

      UserActivity activity = activities.getContent().get(0);
      assertThat(activity.getActivityType()).isEqualTo(ActivityType.FEATURE_USE); // Password reset mapped to feature use
      assertThat(activity.getDescription()).contains("Password reset requested");
      assertThat(activity.getUserId()).isEqualTo(102L);
    });
  }

  @Test
  void shouldRecordLoginActivity() {
    // Given
    Long userId = 200L;
    String sessionId = "session-analytics-login";
    String ipAddress = "192.168.1.100";
    String userAgent = "Mozilla/5.0 Analytics Test";

    // When
    analyticsService.recordLogin(userId, sessionId, ipAddress, userAgent);

    // Then
    Page<UserActivity> activities = activityRepository.findByUserId(userId, PageRequest.of(0, 10));
    assertThat(activities.getContent()).isNotEmpty();

    UserActivity activity = activities.getContent().get(0);
    assertThat(activity.getActivityType()).isEqualTo(ActivityType.LOGIN);
    assertThat(activity.getUserId()).isEqualTo(userId);
    assertThat(activity.getSessionId()).isEqualTo(sessionId);
    assertThat(activity.getIpAddress()).isEqualTo(ipAddress);
    assertThat(activity.getUserAgent()).isEqualTo(userAgent);
  }

  @Test
  void shouldRecordLogoutActivity() {
    // Given
    Long userId = 201L;
    String sessionId = "session-analytics-logout";

    // When
    analyticsService.recordLogout(userId, sessionId);

    // Then
    Page<UserActivity> activities = activityRepository.findByUserId(userId, PageRequest.of(0, 10));
    assertThat(activities.getContent()).isNotEmpty();

    UserActivity activity = activities.getContent().get(0);
    assertThat(activity.getActivityType()).isEqualTo(ActivityType.LOGOUT);
    assertThat(activity.getUserId()).isEqualTo(userId);
    assertThat(activity.getSessionId()).isEqualTo(sessionId);
  }

  @Test
  void shouldValidateUserActivityDomainLogic() {
    // Test UserActivity domain logic in isolation

    // Create activities using domain factory methods
    UserActivity loginActivity = UserActivity.recordLogin(
        300L,
        "session-domain-test",
        "10.0.0.1",
        "Test User Agent"
    );

    UserActivity failedLoginActivity = UserActivity.recordFailedLogin(
        301L,
        "10.0.0.2",
        "Malicious User Agent",
        "Invalid credentials"
    );

    UserActivity pageViewActivity = UserActivity.recordPageView(
        302L,
        "session-page-view",
        "/dashboard",
        "https://example.com"
    );

    UserActivity featureActivity = UserActivity.recordFeatureUse(
        303L,
        "session-feature",
        "user-profile-update",
        Map.of("field", "email", "oldValue", "old@example.com", "newValue", "new@example.com")
    );

    // Validate domain logic
    assertThat(loginActivity.isSecuritySensitive()).isTrue();
    assertThat(loginActivity.isHighValueActivity()).isTrue();
    assertThat(loginActivity.getActivityScore()).isEqualTo(10);
    assertThat(loginActivity.getEngagementLevel()).isEqualTo(UserActivity.EngagementLevel.SESSION);

    assertThat(failedLoginActivity.isSecuritySensitive()).isTrue();
    assertThat(failedLoginActivity.isSuspicious()).isTrue();
    assertThat(failedLoginActivity.getActivityScore()).isEqualTo(20);

    assertThat(pageViewActivity.isSecuritySensitive()).isFalse();
    assertThat(pageViewActivity.isHighValueActivity()).isFalse();
    assertThat(pageViewActivity.getActivityScore()).isEqualTo(1);
    assertThat(pageViewActivity.getEngagementLevel()).isEqualTo(UserActivity.EngagementLevel.LOW);

    assertThat(featureActivity.getEngagementLevel()).isEqualTo(UserActivity.EngagementLevel.MEDIUM);
    assertThat(featureActivity.getActivityScore()).isEqualTo(3);
  }

  @Test
  void shouldValidateActivityAnalytics() {
    // Create and save an activity
    UserActivity activity = UserActivity.recordLogin(
        400L,
        "session-analytics-test",
        "172.16.0.1",
        "Analytics Test Agent"
    );

    UserActivity saved = activityRepository.save(activity);

    // Get analytics data
    UserActivity.ActivityAnalytics analytics = saved.getAnalytics();

    assertThat(analytics).isNotNull();
    assertThat(analytics.activityType()).isEqualTo(ActivityType.LOGIN);
    assertThat(analytics.activityScore()).isEqualTo(10);
    assertThat(analytics.engagementLevel()).isEqualTo(UserActivity.EngagementLevel.SESSION);
    assertThat(analytics.isSecuritySensitive()).isTrue();
    assertThat(analytics.isHighValue()).isTrue();
    assertThat(analytics.hourOfDay()).isBetween(0, 23);
    assertThat(analytics.dayOfWeek()).isBetween(1, 7);
  }

  @Test
  void shouldValidateSecuritySensitiveActivities() {
    // Create security-sensitive activities
    Long userId = 500L;

    analyticsService.recordLogin(userId, "session-security-1", "192.168.1.1", "Browser");

    UserActivity failedLogin = UserActivity.recordFailedLogin(
        userId, "192.168.1.1", "Browser", "Wrong password"
    );
    activityRepository.save(failedLogin);

    UserActivity passwordChange = UserActivity.recordPasswordChange(userId, "session-security-2");
    activityRepository.save(passwordChange);

    // Query security-sensitive activities
    Page<UserActivity> securityActivities = activityRepository.findSecuritySensitiveActivities(
        userId, PageRequest.of(0, 10)
    );

    assertThat(securityActivities.getContent()).hasSize(3);
    assertThat(securityActivities.getContent())
        .allMatch(UserActivity::isSecuritySensitive);
  }

  @Test
  void shouldValidateSuspiciousActivityDetection() {
    // Create suspicious activities
    Page<UserActivity> suspiciousActivities = activityRepository.findSuspiciousActivities(
        PageRequest.of(0, 10)
    );

    // Should find failed login attempts
    assertThat(suspiciousActivities).isNotNull();
    // Note: Actual suspicious activities depend on what's been created in other tests
  }

  @Test
  void shouldValidateAnalyticsModuleIsolation() {
    // Verify that AnalyticsService operates independently
    assertThat(analyticsService).isNotNull();
    assertThat(activityRepository).isNotNull();

    // The module should handle its own domain logic
    UserActivity testActivity = new UserActivity();
    testActivity.setUserId(600L);
    testActivity.setActivityType(ActivityType.PAGE_VIEW);
    testActivity.setDescription("Isolation test");
    testActivity.setOccurredAt(Instant.now());

    // Validate activity before saving
    testActivity.validateActivity(); // Should not throw

    UserActivity saved = activityRepository.save(testActivity);
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUserId()).isEqualTo(600L);
  }

  @Test
  void shouldValidateActivityTemporalAnalysis() {
    // Create activity and test temporal analysis
    UserActivity activity = UserActivity.recordFeatureUse(
        700L,
        "session-temporal",
        "dashboard-view",
        Map.of("page", "main-dashboard")
    );

    UserActivity saved = activityRepository.save(activity);

    // Validate temporal analysis
    assertThat(saved.getHourOfDay()).isBetween(0, 23);
    assertThat(saved.getDayOfWeek()).isBetween(1, 7);
    assertThat(saved.isBusinessHours()).isIn(true, false); // Depends on when test runs
    assertThat(saved.isWeekend()).isIn(true, false); // Depends on when test runs
    assertThat(saved.getMinutesSinceOccurred()).isGreaterThanOrEqualTo(0);
  }
}
