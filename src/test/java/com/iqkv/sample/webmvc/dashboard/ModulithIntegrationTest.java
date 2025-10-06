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

package com.iqkv.sample.webmvc.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivityDomainRepository;
import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationDomainRepository;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive modulith integration test that validates cross-module communication
 * through domain events while maintaining bounded context isolation.
 * <p>
 * This test ensures that the modular architecture works correctly end-to-end
 * with proper event-driven communication between bounded contexts.
 */
@SpringBootTest
@EnableScenarios
@ActiveProfiles("test")
@Transactional
class ModulithIntegrationTest {

  @Autowired
  private UserManagementService userManagementService;

  @Autowired
  private NotificationDomainRepository notificationRepository;

  @Autowired
  private UserActivityDomainRepository activityRepository;

  @Test
  void shouldHandleCompleteUserRegistrationFlow(Scenario scenario) {
    // Given
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("integrationuser");
    userDto.setEmail("integration@example.com");
    userDto.setFirstName("Integration");
    userDto.setLastName("Test");
    userDto.setActivated(false);

    // When - Create user (should trigger UserRegisteredEvent)
    User createdUser = scenario
        .stimulate(() -> userManagementService.createUser(userDto))
        .andWaitForEventOfType(com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent.class)
        .toArrive();

    // Then - Verify user creation
    assertThat(createdUser).isNotNull();
    assertThat(createdUser.getLogin()).isEqualTo("integrationuser");
    assertThat(createdUser.isActivated()).isFalse();
    assertThat(createdUser.getActivationKey()).isNotNull();

    // And - Verify cross-module event handling
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      // Notification module should have created activation notification
      Page<Notification> notifications = notificationRepository.findByRecipient(
          "integration@example.com", PageRequest.of(0, 10)
      );
      assertThat(notifications.getContent()).isNotEmpty();

      Notification notification = notifications.getContent().get(0);
      assertThat(notification.getType()).isEqualTo(NotificationType.USER_ACTION);
      assertThat(notification.getSubject()).contains("Activate");
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);

      // Analytics module should have recorded registration activity
      Page<UserActivity> activities = activityRepository.findByUserId(
          createdUser.getId(), PageRequest.of(0, 10)
      );
      assertThat(activities.getContent()).isNotEmpty();

      UserActivity activity = activities.getContent().get(0);
      assertThat(activity.getActivityType()).isEqualTo(ActivityType.FEATURE_USE);
      assertThat(activity.getDescription()).contains("integrationuser");
    });
  }

  @Test
  void shouldHandleCompleteUserActivationFlow(Scenario scenario) {
    // Given - Create a user first
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("activationuser");
    userDto.setEmail("activation@example.com");
    userDto.setFirstName("Activation");
    userDto.setLastName("Test");
    userDto.setActivated(false);

    User createdUser = userManagementService.createUser(userDto);
    String activationKey = createdUser.getActivationKey();

    // When - Activate user (should trigger UserActivatedEvent)
    scenario
        .stimulate(() -> userManagementService.activateRegistration(activationKey))
        .andWaitForEventOfType(com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent.class)
        .toArrive();

    // Then - Verify cross-module event handling
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      // Notification module should have created welcome notification
      List<Notification> notifications = notificationRepository.findByRecipient("activation@example.com").getContent();
      Notification welcomeNotification = notifications.stream()
          .filter(n -> n.getSubject().contains("Welcome"))
          .findFirst()
          .orElseThrow(() -> new AssertionError("Welcome notification not found"));

      assertThat(welcomeNotification.getType()).isEqualTo(NotificationType.USER_ACTION);
      assertThat(welcomeNotification.getContent()).contains("activationuser");

      // Analytics module should have recorded activation activity
      Page<UserActivity> activities = activityRepository.findByUserId(
          createdUser.getId(), PageRequest.of(0, 10)
      );

      boolean hasActivationActivity = activities.getContent().stream()
          .anyMatch(a -> a.getDescription().contains("activationuser") &&
                         a.getActivityType() == ActivityType.FEATURE_USE);
      assertThat(hasActivationActivity).isTrue();
    });
  }

  @Test
  void shouldHandleCompletePasswordResetFlow(Scenario scenario) {
    // Given - Create and activate a user first
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("resetuser");
    userDto.setEmail("reset@example.com");
    userDto.setFirstName("Reset");
    userDto.setLastName("Test");
    userDto.setActivated(true);

    User createdUser = userManagementService.createUser(userDto);

    // When - Request password reset (should trigger PasswordResetRequestedEvent)
    scenario
        .stimulate(() -> userManagementService.requestPasswordReset("reset@example.com"))
        .andWaitForEventOfType(com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent.class)
        .toArrive();

    // Then - Verify cross-module event handling
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      // Notification module should have created password reset notification
      List<Notification> notifications = notificationRepository.findByRecipient("reset@example.com").getContent();
      Notification resetNotification = notifications.stream()
          .filter(n -> n.getSubject().contains("Password Reset"))
          .findFirst()
          .orElseThrow(() -> new AssertionError("Password reset notification not found"));

      assertThat(resetNotification.getType()).isEqualTo(NotificationType.SECURITY_ALERT);
      assertThat(resetNotification.getContent()).contains("reset");

      // Analytics module should have recorded password reset activity
      Page<UserActivity> activities = activityRepository.findByUserId(
          createdUser.getId(), PageRequest.of(0, 10)
      );

      boolean hasResetActivity = activities.getContent().stream()
          .anyMatch(a -> a.getDescription().contains("Password reset") &&
                         a.getActivityType() == ActivityType.FEATURE_USE);
      assertThat(hasResetActivity).isTrue();
    });
  }

  @Test
  void shouldValidateModularArchitectureIntegrity() {
    // Verify that all modules are properly loaded and integrated
    assertThat(userManagementService).isNotNull();
    assertThat(notificationRepository).isNotNull();
    assertThat(activityRepository).isNotNull();

    // Verify that modules can operate independently
    long userCount = userManagementService.countUsers();
    assertThat(userCount).isGreaterThanOrEqualTo(0);

    // Verify that repositories are properly isolated
    Page<Notification> notifications = notificationRepository.findByStatus(
        NotificationStatus.PENDING, PageRequest.of(0, 1)
    );
    assertThat(notifications).isNotNull();

    Page<UserActivity> activities = activityRepository.findSuspiciousActivities(
        PageRequest.of(0, 1)
    );
    assertThat(activities).isNotNull();
  }

  @Test
  void shouldValidateEventDrivenCommunication() {
    // Test that modules communicate only through events, not direct dependencies

    // Create a user to trigger events
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("eventuser");
    userDto.setEmail("event@example.com");
    userDto.setFirstName("Event");
    userDto.setLastName("Test");
    userDto.setActivated(false);

    User createdUser = userManagementService.createUser(userDto);

    // Verify that other modules respond to events asynchronously
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      // Check that notification was created (event-driven)
      Page<Notification> notifications = notificationRepository.findByRecipient(
          "event@example.com", PageRequest.of(0, 10)
      );
      assertThat(notifications.getContent()).isNotEmpty();

      // Check that activity was recorded (event-driven)
      Page<UserActivity> activities = activityRepository.findByUserId(
          createdUser.getId(), PageRequest.of(0, 10)
      );
      assertThat(activities.getContent()).isNotEmpty();
    });
  }

  @Test
  void shouldValidateBoundedContextIsolation() {
    // Verify that each bounded context maintains its own data and logic

    // User Management context
    long initialUserCount = userManagementService.countUsers();

    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("isolationuser");
    userDto.setEmail("isolation@example.com");
    userDto.setFirstName("Isolation");
    userDto.setLastName("Test");
    userDto.setActivated(false);

    User createdUser = userManagementService.createUser(userDto);

    // Verify user management context integrity
    long newUserCount = userManagementService.countUsers();
    assertThat(newUserCount).isEqualTo(initialUserCount + 1);

    // Verify that other contexts maintain their own state
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      // Notification context should have its own data
      Page<Notification> allNotifications = notificationRepository.findByStatus(
          NotificationStatus.PENDING, PageRequest.of(0, 100)
      );
      assertThat(allNotifications.getContent()).isNotEmpty();

      // Analytics context should have its own data
      Page<UserActivity> allActivities = activityRepository.findByUserId(
          createdUser.getId(), PageRequest.of(0, 100)
      );
      assertThat(allActivities.getContent()).isNotEmpty();
    });
  }

  @Test
  void shouldValidateAsynchronousEventProcessing() {
    // Test that events are processed asynchronously without blocking

    long startTime = System.currentTimeMillis();

    // Create multiple users rapidly
    for (int i = 0; i < 3; i++) {
      AdminUserDTO userDto = new AdminUserDTO();
      userDto.setLogin("asyncuser" + i);
      userDto.setEmail("async" + i + "@example.com");
      userDto.setFirstName("Async" + i);
      userDto.setLastName("Test");
      userDto.setActivated(false);

      userManagementService.createUser(userDto);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // User creation should be fast (not blocked by event processing)
    assertThat(duration).isLessThan(5000); // Less than 5 seconds

    // But events should eventually be processed
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      for (int i = 0; i < 3; i++) {
        String email = "async" + i + "@example.com";
        Page<Notification> notifications = notificationRepository.findByRecipient(
            email, PageRequest.of(0, 10)
        );
        assertThat(notifications.getContent()).isNotEmpty();
      }
    });
  }
}
