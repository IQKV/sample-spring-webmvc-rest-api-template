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

import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.notification.infrastructure.NotificationRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for notification operations.
 * Handles cross-bounded context communication by listening to domain events.
 */
@Service
@Transactional
public class NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * Handles user registration events by creating activation notifications.
   *
   * @param event the user registered event
   */
  @EventListener
  @Async
  public void handleUserRegistered(UserRegisteredEvent event) {
    LOG.info("Creating activation notification for user: {}", event.login());

    Notification notification = new Notification();
    notification.setRecipient(event.email());
    notification.setSubject("Activate your account");
    notification.setContent(createActivationEmailContent(event.login(), event.activationKey()));
    notification.setType(NotificationType.ACTIVATION);
    notification.setChannel(NotificationChannel.EMAIL);

    notificationRepository.save(notification);

    // In a real implementation, you would trigger the actual sending here
    // or have a separate process that picks up pending notifications
    LOG.debug("Activation notification created for: {}", event.email());
  }

  /**
   * Handles user activation events by creating welcome notifications.
   *
   * @param event the user activated event
   */
  @EventListener
  @Async
  public void handleUserActivated(UserActivatedEvent event) {
    LOG.info("Creating welcome notification for user: {}", event.login());

    Notification notification = new Notification();
    notification.setRecipient(event.email());
    notification.setSubject("Welcome to our platform!");
    notification.setContent(createWelcomeEmailContent(event.login()));
    notification.setType(NotificationType.WELCOME);
    notification.setChannel(NotificationChannel.EMAIL);

    notificationRepository.save(notification);

    LOG.debug("Welcome notification created for: {}", event.email());
  }

  /**
   * Handles password reset events by creating reset notifications.
   *
   * @param event the password reset requested event
   */
  @EventListener
  @Async
  public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
    LOG.info("Creating password reset notification for user with email: {}", event.email());

    Notification notification = new Notification();
    notification.setRecipient(event.email());
    notification.setSubject("Password Reset Request");
    notification.setContent(createPasswordResetEmailContent(event.resetKey()));
    notification.setType(NotificationType.PASSWORD_RESET);
    notification.setChannel(NotificationChannel.EMAIL);

    notificationRepository.save(notification);

    LOG.debug("Password reset notification created for: {}", event.email());
  }

  private String createActivationEmailContent(String login, String activationKey) {
    return String.format(
        "Hello %s,\n\n" +
        "Thank you for registering with our platform. " +
        "Please click the following link to activate your account:\n\n" +
        "http://localhost:8080/activate?key=%s\n\n" +
        "Best regards,\n" +
        "The Team",
        login, activationKey
    );
  }

  private String createWelcomeEmailContent(String login) {
    return String.format(
        "Hello %s,\n\n" +
        "Welcome to our platform! Your account has been successfully activated.\n" +
        "You can now start using all the features available to you.\n\n" +
        "If you have any questions, please don't hesitate to contact our support team.\n\n" +
        "Best regards,\n" +
        "The Team",
        login
    );
  }

  private String createPasswordResetEmailContent(String resetKey) {
    return String.format(
        "Hello,\n\n" +
        "You have requested to reset your password. " +
        "Please click the following link to reset your password:\n\n" +
        "http://localhost:8080/reset-password?key=%s\n\n" +
        "If you did not request this password reset, please ignore this email.\n\n" +
        "Best regards,\n" +
        "The Team",
        resetKey
    );
  }
}
