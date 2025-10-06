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

package com.iqkv.sample.webmvc.dashboard.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.notification.infrastructure.NotificationRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for NotificationService within the Notification bounded context.
 * <p>
 * Tests the application layer's orchestration of notification workflows,
 * event handling, and cross-bounded context integration.
 */
@SpringBootTest
@IntegrationTest
@TestPropertySource(properties = {
    "application.mail.enabled=false" // Disable actual email sending
})
class NotificationServiceIT {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @AfterEach
  void tearDown() {
    notificationRepository.deleteAll();
  }

  @Test
  @Transactional
  void shouldCreateWelcomeNotificationOnUserRegistration() {
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
      List<Notification> notifications = notificationRepository.findByRecipient("test@example.com");
      assertThat(notifications).hasSize(1);

      Notification notification = notifications.get(0);
      assertThat(notification.getType()).isEqualTo(NotificationType.WELCOME);
      assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
      assertThat(notification.getSubject()).contains("Welcome");
      assertThat(notification.getContent()).contains("Test User");
    });
  }

  @Test
  @Transactional
  void shouldCreateActivationNotificationOnUserRegistration() {
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
      List<Notification> notifications = notificationRepository.findByRecipient("test@example.com");
      assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);

      boolean hasActivationNotification = notifications.stream()
          .anyMatch(n -> n.getType() == NotificationType.ACTIVATION);
      assertThat(hasActivationNotification).isTrue();
    });
  }

  @Test
  @Transactional
  void shouldCreateConfirmationNotificationOnUserActivation() {
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
      List<Notification> notifications = notificationRepository.findByRecipient("test@example.com");
      assertThat(notifications).hasSize(1);

      Notification notification = notifications.get(0);
      assertThat(notification.getType()).isEqualTo(NotificationType.CONFIRMATION);
      assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
      assertThat(notification.getSubject()).contains("Account Activated");
    });
  }

  @Test
  @Transactional
  void shouldSendPendingNotifications() {
    // Given
    Notification notification = createTestNotification();
    notificationRepository.save(notification);

    // When
    notificationService.sendPendingNotifications();

    // Then
    Notification sentNotification = notificationRepository.findById(notification.getId()).orElseThrow();
    assertThat(sentNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(sentNotification.getSentAt()).isNotNull();
  }

  @Test
  @Transactional
  void shouldRetryFailedNotifications() {
    // Given
    Notification notification = createTestNotification();
    notification.markAsFailed("Network error");
    notificationRepository.save(notification);

    // When
    notificationService.retryFailedNotifications();

    // Then
    Notification retriedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
    assertThat(retriedNotification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(retriedNotification.getErrorMessage()).isNull();
  }

  @Test
  @Transactional
  void shouldNotRetryNotificationsExceedingMaxAttempts() {
    // Given
    Notification notification = createTestNotification();
    notification.setRetryCount(3); // Max attempts
    notification.markAsFailed("Max attempts reached");
    notificationRepository.save(notification);

    // When
    notificationService.retryFailedNotifications();

    // Then
    Notification unchangedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
    assertThat(unchangedNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    assertThat(unchangedNotification.getRetryCount()).isEqualTo(4); // Incremented by markAsFailed
  }

  @Test
  @Transactional
  void shouldCleanupOldNotifications() {
    // Given
    Notification oldNotification = createTestNotification();
    oldNotification.markAsSent();
    // Simulate old notification by setting created date in the past
    oldNotification.setCreatedDate(java.time.Instant.now().minus(java.time.Duration.ofDays(31)));
    notificationRepository.save(oldNotification);

    Notification recentNotification = createTestNotification();
    recentNotification.setRecipient("recent@example.com");
    notificationRepository.save(recentNotification);

    // When
    notificationService.cleanupOldNotifications();

    // Then
    assertThat(notificationRepository.findById(oldNotification.getId())).isEmpty();
    assertThat(notificationRepository.findById(recentNotification.getId())).isPresent();
  }

  @Test
  @Transactional
  void shouldGetNotificationsByRecipient() {
    // Given
    String recipient = "test@example.com";
    Notification notification1 = createTestNotification();
    notification1.setRecipient(recipient);
    notification1.setType(NotificationType.WELCOME);

    Notification notification2 = createTestNotification();
    notification2.setRecipient(recipient);
    notification2.setType(NotificationType.ACTIVATION);

    Notification notification3 = createTestNotification();
    notification3.setRecipient("other@example.com");

    notificationRepository.saveAll(List.of(notification1, notification2, notification3));

    // When
    List<Notification> notifications = notificationService.getNotificationsByRecipient(recipient);

    // Then
    assertThat(notifications).hasSize(2);
    assertThat(notifications).extracting(Notification::getRecipient)
        .containsOnly(recipient);
  }

  private Notification createTestNotification() {
    Notification notification = new Notification();
    notification.setRecipient("test@example.com");
    notification.setSubject("Test Subject");
    notification.setContent("Test Content");
    notification.setType(NotificationType.WELCOME);
    notification.setChannel(NotificationChannel.EMAIL);
    notification.setStatus(NotificationStatus.PENDING);
    notification.setRetryCount(0);
    return notification;
  }
}
