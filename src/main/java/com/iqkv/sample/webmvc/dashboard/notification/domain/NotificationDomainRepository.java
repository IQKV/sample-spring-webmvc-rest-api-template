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

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain repository interface for Notification aggregate.
 * <p>
 * This interface defines the contract for Notification persistence operations
 * from the domain perspective, without exposing infrastructure concerns.
 */
public interface NotificationDomainRepository {

  /**
   * Saves a notification.
   *
   * @param notification the notification to save
   * @return the saved notification
   */
  Notification save(Notification notification);

  /**
   * Finds a notification by ID.
   *
   * @param id the notification ID
   * @return the notification if found
   */
  Optional<Notification> findById(Long id);

  /**
   * Finds all notifications for a specific recipient.
   *
   * @param recipient the recipient
   * @param pageable  pagination information
   * @return page of notifications
   */
  Page<Notification> findByRecipient(String recipient, Pageable pageable);

  /**
   * Finds notifications by status.
   *
   * @param status   the notification status
   * @param pageable pagination information
   * @return page of notifications
   */
  Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

  /**
   * Finds notifications by type and status.
   *
   * @param type     the notification type
   * @param status   the notification status
   * @param pageable pagination information
   * @return page of notifications
   */
  Page<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, Pageable pageable);

  /**
   * Finds notifications by channel and status.
   *
   * @param channel  the notification channel
   * @param status   the notification status
   * @param pageable pagination information
   * @return page of notifications
   */
  Page<Notification> findByChannelAndStatus(NotificationChannel channel, NotificationStatus status, Pageable pageable);

  /**
   * Finds pending notifications ready for sending.
   *
   * @param pageable pagination information
   * @return page of pending notifications
   */
  Page<Notification> findPendingNotifications(Pageable pageable);

  /**
   * Finds failed notifications ready for retry.
   *
   * @param currentTime the current time for retry calculation
   * @param pageable    pagination information
   * @return page of notifications ready for retry
   */
  Page<Notification> findFailedNotificationsReadyForRetry(Instant currentTime, Pageable pageable);

  /**
   * Finds high-priority notifications.
   *
   * @param pageable pagination information
   * @return page of high-priority notifications
   */
  Page<Notification> findHighPriorityNotifications(Pageable pageable);

  /**
   * Finds overdue notifications.
   *
   * @param currentTime the current time
   * @param pageable    pagination information
   * @return page of overdue notifications
   */
  Page<Notification> findOverdueNotifications(Instant currentTime, Pageable pageable);

  /**
   * Finds expired notifications.
   *
   * @param expiryTime the expiry cutoff time
   * @param pageable   pagination information
   * @return page of expired notifications
   */
  Page<Notification> findExpiredNotifications(Instant expiryTime, Pageable pageable);

  /**
   * Counts notifications by status for a recipient.
   *
   * @param recipient the recipient
   * @param status    the notification status
   * @return count of notifications
   */
  long countByRecipientAndStatus(String recipient, NotificationStatus status);

  /**
   * Counts notifications by type and status within a time range.
   *
   * @param type      the notification type
   * @param status    the notification status
   * @param startTime the start time
   * @param endTime   the end time
   * @return count of notifications
   */
  long countByTypeAndStatusAndCreatedDateBetween(NotificationType type, NotificationStatus status, Instant startTime, Instant endTime);

  /**
   * Deletes old notifications before a specific date.
   *
   * @param cutoffDate the cutoff date
   * @return number of deleted notifications
   */
  long deleteNotificationsOlderThan(Instant cutoffDate);

  /**
   * Finds notifications created within a time range.
   *
   * @param startTime the start time
   * @param endTime   the end time
   * @param pageable  pagination information
   * @return page of notifications
   */
  Page<Notification> findByCreatedDateBetween(Instant startTime, Instant endTime, Pageable pageable);

  /**
   * Finds the most recent notification for a recipient.
   *
   * @param recipient the recipient
   * @return the most recent notification if found
   */
  Optional<Notification> findMostRecentByRecipient(String recipient);
}
