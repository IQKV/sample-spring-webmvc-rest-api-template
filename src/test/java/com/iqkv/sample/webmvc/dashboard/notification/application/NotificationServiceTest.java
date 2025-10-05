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
import static org.mockito.Mockito.verify;

import java.time.Instant;

import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.notification.infrastructure.NotificationRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for NotificationService.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService = new NotificationService(notificationRepository);
  }

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
    notificationService.handleUserRegistered(event);

    // Then
    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());

    Notification savedNotification = notificationCaptor.getValue();
    assertThat(savedNotification.getRecipient()).isEqualTo(event.email());
    assertThat(savedNotification.getSubject()).isEqualTo("Activate your account");
    assertThat(savedNotification.getContent()).contains(event.login());
    assertThat(savedNotification.getContent()).contains(event.activationKey());
    assertThat(savedNotification.getType()).isEqualTo(NotificationType.ACTIVATION);
    assertThat(savedNotification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
  }

  @Test
  void shouldHandleUserActivatedEvent() {
    // Given
    UserActivatedEvent event = new UserActivatedEvent(
        1L,
        "testuser",
        "test@example.com",
        Instant.now()
    );

    // When
    notificationService.handleUserActivated(event);

    // Then
    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());

    Notification savedNotification = notificationCaptor.getValue();
    assertThat(savedNotification.getRecipient()).isEqualTo(event.email());
    assertThat(savedNotification.getSubject()).isEqualTo("Welcome to our platform!");
    assertThat(savedNotification.getContent()).contains(event.login());
    assertThat(savedNotification.getType()).isEqualTo(NotificationType.WELCOME);
    assertThat(savedNotification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
  }

  @Test
  void shouldHandlePasswordResetRequestedEvent() {
    // Given
    PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
        1L,
        "test@example.com",
        "reset-key-123",
        Instant.now()
    );

    // When
    notificationService.handlePasswordResetRequested(event);

    // Then
    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());

    Notification savedNotification = notificationCaptor.getValue();
    assertThat(savedNotification.getRecipient()).isEqualTo(event.email());
    assertThat(savedNotification.getSubject()).isEqualTo("Password Reset Request");
    assertThat(savedNotification.getContent()).contains(event.resetKey());
    assertThat(savedNotification.getType()).isEqualTo(NotificationType.PASSWORD_RESET);
    assertThat(savedNotification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
  }
}
