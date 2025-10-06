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

package com.iqkv.sample.webmvc.dashboard.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Notification domain entity.
 */
class NotificationTest {

  private Notification notification;

  @BeforeEach
  void setUp() {
    notification = new Notification();
    notification.setId(1L);
    notification.setRecipient("test@example.com");
    notification.setSubject("Test Subject");
    notification.setContent("Test Content");
    notification.setType(NotificationType.ACTIVATION);
    notification.setChannel(NotificationChannel.EMAIL);
    notification.setStatus(NotificationStatus.PENDING);
    notification.setRetryCount(0);
  }

  @Test
  void shouldMarkAsSent() {
    // Given
    Instant beforeSent = Instant.now();

    // When
    notification.markAsSent();

    // Then
    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(notification.getSentAt()).isNotNull();
    assertThat(notification.getSentAt()).isAfterOrEqualTo(beforeSent);
    assertThat(notification.getErrorMessage()).isNull();
  }

  @Test
  void shouldThrowExceptionWhenMarkingAlreadySentNotificationAsSent() {
    // Given
    notification.setStatus(NotificationStatus.SENT);

    // When & Then
    assertThatThrownBy(() -> notification.markAsSent())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Notification is already sent");
  }

  @Test
  void shouldMarkAsFailed() {
    // Given
    String errorMessage = "Failed to send email";

    // When
    notification.markAsFailed(errorMessage);

    // Then
    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    assertThat(notification.getErrorMessage()).isEqualTo(errorMessage);
    assertThat(notification.getRetryCount()).isEqualTo(1);
  }

  @Test
  void shouldIncrementRetryCountWhenMarkingAsFailed() {
    // Given
    notification.setRetryCount(2);
    String errorMessage = "Network error";

    // When
    notification.markAsFailed(errorMessage);

    // Then
    assertThat(notification.getRetryCount()).isEqualTo(3);
  }

  @Test
  void shouldRetryFailedNotification() {
    // Given
    notification.setStatus(NotificationStatus.FAILED);
    notification.setErrorMessage("Previous error");
    notification.setRetryCount(1);

    // When
    notification.retry();

    // Then
    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(notification.getErrorMessage()).isNull();
    assertThat(notification.getRetryCount()).isEqualTo(1); // Retry count is not reset
  }

  @Test
  void shouldThrowExceptionWhenRetryingNonFailedNotification() {
    // Given
    notification.setStatus(NotificationStatus.PENDING);

    // When & Then
    assertThatThrownBy(() -> notification.retry())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Only failed notifications can be retried");
  }

  @Test
  void shouldThrowExceptionWhenRetryingAfterMaxAttempts() {
    // Given
    notification.setStatus(NotificationStatus.FAILED);
    notification.setRetryCount(3); // Max attempts reached

    // When & Then
    assertThatThrownBy(() -> notification.retry())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Maximum retry attempts exceeded");
  }

  @Test
  void shouldCheckIfCanRetry() {
    // Given - Failed notification with retry count < 3
    notification.setStatus(NotificationStatus.FAILED);
    notification.setRetryCount(2);

    // When & Then
    assertThat(notification.canRetry()).isTrue();

    // Given - Failed notification with retry count = 3
    notification.setRetryCount(3);

    // When & Then
    assertThat(notification.canRetry()).isFalse();

    // Given - Sent notification
    notification.setStatus(NotificationStatus.SENT);
    notification.setRetryCount(1);

    // When & Then
    assertThat(notification.canRetry()).isFalse();

    // Given - Pending notification
    notification.setStatus(NotificationStatus.PENDING);
    notification.setRetryCount(1);

    // When & Then
    assertThat(notification.canRetry()).isFalse();
  }

  @Test
  void shouldTestEqualsAndHashCode() {
    // Given
    Notification notification1 = new Notification();
    notification1.setId(1L);

    Notification notification2 = new Notification();
    notification2.setId(1L);

    Notification notification3 = new Notification();
    notification3.setId(2L);

    // When & Then
    assertThat(notification1).isEqualTo(notification2);
    assertThat(notification1).isNotEqualTo(notification3);
    assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
  }

  @Test
  void shouldTestToString() {
    // When
    String result = notification.toString();

    // Then
    assertThat(result).contains("Notification{");
    assertThat(result).contains("id=1");
    assertThat(result).contains("recipient='test@example.com'");
    assertThat(result).contains("subject='Test Subject'");
    assertThat(result).contains("type=ACTIVATION");
    assertThat(result).contains("channel=EMAIL");
    assertThat(result).contains("status=PENDING");
  }
}
