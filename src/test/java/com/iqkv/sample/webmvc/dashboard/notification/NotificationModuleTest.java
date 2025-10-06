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

package com.iqkv.sample.webmvc.dashboard.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.iqkv.sample.webmvc.dashboard.notification.application.NotificationService;
import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationDomainRepository;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Modulith integration test for Notification bounded context.
 * <p>
 * This test validates that the Notification module works correctly in isolation
 * and properly handles domain events from other bounded contexts.
 */
@ApplicationModuleTest
@ActiveProfiles("test")
class NotificationModuleTest {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private NotificationDomainRepository notificationRepository;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Test
  void shouldHandleUserRegisteredEvent() {
    // Given
    UserRegisteredEvent event = new UserRegisteredEvent(
        1L,
        "testuser",
        "test@example.com",
        "activation-key-123",
        Instant.now()
    );

    // When
    eventPublisher.publishEvent(event);

    // Then - Wait for async event processing
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      List<Notification> notifications = notificationRepository.findByRecipient("test@example.com").getContent();
      assertThat(notifications).isNotEmpty();

      Notification notification = notifications.get(0);
      assertThat(notification.getType()).isEqualTo(NotificationType.USER_ACTION);
      assertThat(notification.getSubject()).contains("Activate");
      assertThat(notification.getContent()).contains("activation-key-123");
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    });
  }

  @Test
  void shouldHandleUserActivatedEvent() {
    // Given
    UserActivatedEvent event = new UserActivatedEvent(
        2L,
        "activateduser",
        "activated@example.com",
        Instant.now()
    );

    // When
    eventPublisher.publishEvent(event);

    // Then - Wait for async event processing
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      List<Notification> notifications = notificationRepository.findByRecipient("activated@example.com").getContent();
      assertThat(notifications).isNotEmpty();

      Notification notification = notifications.get(0);
      assertThat(notification.getType()).isEqualTo(NotificationType.USER_ACTION);
      assertThat(notification.getSubject()).contains("Welcome");
      assertThat(notification.getContent()).contains("activateduser");
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    });
  }

  @Test
  void shouldHandlePasswordResetRequestedEvent() {
    // Given
    PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
        3L,
        "resetuser@example.com",
        "reset-key-456",
        Instant.now()
    );

    // When
    eventPublisher.publishEvent(event);

    // Then - Wait for async event processing
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      List<Notification> notifications = notificationRepository.findByRecipient("resetuser@example.com").getContent();
      assertThat(notifications).isNotEmpty();

      Notification notification = notifications.get(0);
      assertThat(notification.getType()).isEqualTo(NotificationType.SECURITY_ALERT);
      assertThat(notification.getSubject()).contains("Password Reset");
      assertThat(notification.getContent()).contains("reset-key-456");
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    });
  }

  @Test
  void shouldValidateNotificationDomainLogic() {
    // Test notification domain logic in isolation

    // Create a notification using domain factory method
    Notification notification = Notification.createUserRegistration(
        "factory@example.com",
        "http://localhost:8080/activate?key=test-key"
    );

    // Validate domain logic
    assertThat(notification.getRecipient()).isEqualTo("factory@example.com");
    assertThat(notification.getType()).isEqualTo(NotificationType.USER_ACTION);
    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(notification.getPriority()).isEqualTo(3); // USER_ACTION priority
    assertThat(notification.isHighPriority()).isFalse();

    // Test notification lifecycle
    notification.validateContent(); // Should not throw
    assertThat(notification.canRetry()).isFalse(); // Not failed yet
    assertThat(notification.isExpired()).isFalse(); // Just created
  }

  @Test
  void shouldValidateNotificationRetryLogic() {
    // Create a notification and simulate failure
    Notification notification = Notification.createSecurityAlert(
        "security@example.com",
        "Security breach detected",
        com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel.EMAIL
    );

    // Simulate failure
    notification.markAsFailed("SMTP server unavailable");

    // Validate retry logic
    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    assertThat(notification.canRetry()).isTrue();
    assertThat(notification.getRetryCount()).isEqualTo(1);
    assertThat(notification.getNextRetryTime()).isNotNull();

    // Test retry
    notification.retry();
    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(notification.getErrorMessage()).isNull();
  }

  @Test
  void shouldValidateHighPriorityNotifications() {
    // Create high-priority notifications
    Notification securityAlert = Notification.createSecurityAlert(
        "admin@example.com",
        "Critical security event",
        com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel.EMAIL
    );

    Notification systemMaintenance = Notification.createSystemMaintenance(
        "users@example.com",
        "Scheduled maintenance tonight"
    );

    // Validate priority logic
    assertThat(securityAlert.isHighPriority()).isTrue();
    assertThat(securityAlert.getPriority()).isEqualTo(1);

    assertThat(systemMaintenance.isHighPriority()).isTrue();
    assertThat(systemMaintenance.getPriority()).isEqualTo(2);
  }

  @Test
  void shouldValidateNotificationModuleIsolation() {
    // Verify that NotificationService operates independently
    assertThat(notificationService).isNotNull();
    assertThat(notificationRepository).isNotNull();

    // The module should handle its own domain logic
    Notification testNotification = new Notification();
    testNotification.setRecipient("isolation@example.com");
    testNotification.setSubject("Isolation Test");
    testNotification.setContent("Testing module isolation");
    testNotification.setType(NotificationType.USER_ACTION);
    testNotification.setChannel(com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel.EMAIL);
    testNotification.setStatus(NotificationStatus.PENDING);

    Notification saved = notificationRepository.save(testNotification);
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getRecipient()).isEqualTo("isolation@example.com");
  }

  @Test
  void shouldValidateNotificationDeliveryStats() {
    // Create and save a notification
    Notification notification = Notification.createPasswordReset(
        "stats@example.com",
        "http://localhost:8080/reset?key=stats-key"
    );

    Notification saved = notificationRepository.save(notification);

    // Get delivery statistics
    Notification.NotificationDeliveryStats stats = saved.getDeliveryStats();

    assertThat(stats).isNotNull();
    assertThat(stats.status()).isEqualTo(NotificationStatus.PENDING);
    assertThat(stats.retryCount()).isEqualTo(0);
    assertThat(stats.isExpired()).isFalse();
    assertThat(stats.ageInMinutes()).isGreaterThanOrEqualTo(0);
  }
}
