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

import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;

/**
 * Facade interface for Notification bounded context.
 * Provides a clean contract for other bounded contexts to send notifications.
 */
public interface NotificationFacade {

  /**
   * Send a notification to a user.
   *
   * @param userId  the target user ID
   * @param type    the notification type
   * @param channel the notification channel
   * @param title   the notification title
   * @param message the notification message
   */
  void sendNotification(Long userId, NotificationType type, NotificationChannel channel, String title, String message);

  /**
   * Send an email notification to a user.
   *
   * @param userId  the target user ID
   * @param type    the notification type
   * @param title   the email title
   * @param message the email message
   */
  void sendEmailNotification(Long userId, NotificationType type, String title, String message);

  /**
   * Send a system notification to a user.
   *
   * @param userId  the target user ID
   * @param type    the notification type
   * @param title   the notification title
   * @param message the notification message
   */
  void sendSystemNotification(Long userId, NotificationType type, String title, String message);

  /**
   * Check if notifications are enabled for a user.
   *
   * @param userId  the user ID
   * @param channel the notification channel
   * @return true if notifications are enabled, false otherwise
   */
  boolean areNotificationsEnabled(Long userId, NotificationChannel channel);
}
