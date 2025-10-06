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

import java.util.List;

import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;

/**
 * Service client interface for Notification operations.
 * <p>
 * This interface provides a clean abstraction for accessing notification
 * functionality that can be implemented for both monolithic (local) and
 * microservices (remote) deployment scenarios.
 */
public interface NotificationServiceClient {

  /**
   * Sends a notification.
   *
   * @param recipient the recipient
   * @param subject   the notification subject
   * @param content   the notification content
   * @param type      the notification type
   * @param channel   the notification channel
   * @return the notification ID
   */
  String sendNotification(String recipient, String subject, String content,
                          NotificationType type, NotificationChannel channel);

  /**
   * Gets notifications for a recipient.
   *
   * @param recipient the recipient
   * @param status    the notification status filter (optional)
   * @return list of notifications
   */
  List<NotificationInfo> getNotifications(String recipient, NotificationStatus status);

  /**
   * Gets the count of pending notifications for a recipient.
   *
   * @param recipient the recipient
   * @return count of pending notifications
   */
  long getPendingNotificationCount(String recipient);

  /**
   * Marks a notification as read.
   *
   * @param notificationId the notification ID
   * @return true if successfully marked as read
   */
  boolean markAsRead(String notificationId);

  /**
   * Gets notification delivery statistics.
   *
   * @param recipient the recipient (optional)
   * @return delivery statistics
   */
  NotificationStats getDeliveryStats(String recipient);

  /**
   * Notification information record.
   */
  record NotificationInfo(
      String id,
      String recipient,
      String subject,
      String content,
      NotificationType type,
      NotificationChannel channel,
      NotificationStatus status,
      java.time.Instant createdAt,
      java.time.Instant sentAt
  ) {
  }

  /**
   * Notification statistics record.
   */
  record NotificationStats(
      long totalSent,
      long totalPending,
      long totalFailed,
      double deliveryRate
  ) {
  }
}
