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

package com.iqkv.sample.webmvc.dashboard.notification.presentation;

import java.util.List;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.infrastructure.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for notification management.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);

  private final NotificationRepository notificationRepository;

  public NotificationController(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * Gets all notifications with pagination.
   *
   * @param pageable pagination information
   * @return page of notifications
   */
  @GetMapping
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Page<Notification>> getAllNotifications(Pageable pageable) {
    LOG.debug("REST request to get all notifications");

    Page<Notification> notifications = notificationRepository.findAll(pageable);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Gets notifications by status.
   *
   * @param status the notification status
   * @return list of notifications
   */
  @GetMapping("/status/{status}")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<List<Notification>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
    LOG.debug("REST request to get notifications by status: {}", status);

    List<Notification> notifications = notificationRepository.findByStatus(status);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Gets notifications for a specific recipient.
   *
   * @param recipient the recipient email
   * @return list of notifications
   */
  @GetMapping("/recipient")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<List<Notification>> getNotificationsByRecipient(@RequestParam String recipient) {
    LOG.debug("REST request to get notifications for recipient: {}", recipient);

    List<Notification> notifications = notificationRepository.findByRecipient(recipient);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Gets failed notifications that can be retried.
   *
   * @return list of failed notifications
   */
  @GetMapping("/failed/retryable")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<List<Notification>> getRetryableNotifications() {
    LOG.debug("REST request to get retryable failed notifications");

    List<Notification> notifications = notificationRepository
        .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 3);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Retries a failed notification.
   *
   * @param id the notification ID
   * @return the updated notification
   */
  @PostMapping("/{id}/retry")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Notification> retryNotification(@PathVariable Long id) {
    LOG.debug("REST request to retry notification: {}", id);

    return notificationRepository.findById(id)
        .map(notification -> {
          if (!notification.canRetry()) {
            throw new IllegalStateException("Notification cannot be retried");
          }
          notification.retry();
          Notification savedNotification = notificationRepository.save(notification);
          LOG.debug("Notification {} marked for retry", id);
          return ResponseEntity.ok(savedNotification);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Marks a notification as sent (for testing purposes).
   *
   * @param id the notification ID
   * @return the updated notification
   */
  @PostMapping("/{id}/mark-sent")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Notification> markNotificationAsSent(@PathVariable Long id) {
    LOG.debug("REST request to mark notification as sent: {}", id);

    return notificationRepository.findById(id)
        .map(notification -> {
          notification.markAsSent();
          Notification savedNotification = notificationRepository.save(notification);
          LOG.debug("Notification {} marked as sent", id);
          return ResponseEntity.ok(savedNotification);
        })
        .orElse(ResponseEntity.notFound().build());
  }
}
