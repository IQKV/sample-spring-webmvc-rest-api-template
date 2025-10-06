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

package com.iqkv.sample.webmvc.dashboard.notification.infrastructure;

import java.time.Instant;
import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationDomainRepository;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Repository adapter that implements the domain repository interface
 * and delegates to the JPA repository.
 */
@Component
public class NotificationRepositoryAdapter implements NotificationDomainRepository {

  private final NotificationRepository notificationRepository;

  public NotificationRepositoryAdapter(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override
  public Notification save(Notification notification) {
    return notificationRepository.save(notification);
  }

  @Override
  public Optional<Notification> findById(Long id) {
    return notificationRepository.findById(id);
  }

  @Override
  public Page<Notification> findByRecipient(String recipient, Pageable pageable) {
    return notificationRepository.findByRecipient(recipient, pageable);
  }

  @Override
  public Page<Notification> findByStatus(NotificationStatus status, Pageable pageable) {
    return notificationRepository.findByStatus(status, pageable);
  }

  @Override
  public Page<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, Pageable pageable) {
    return notificationRepository.findByTypeAndStatus(type, status, pageable);
  }

  @Override
  public Page<Notification> findByChannelAndStatus(NotificationChannel channel, NotificationStatus status, Pageable pageable) {
    return notificationRepository.findByChannelAndStatus(channel, status, pageable);
  }

  @Override
  public Page<Notification> findPendingNotifications(Pageable pageable) {
    return notificationRepository.findByStatus(NotificationStatus.PENDING, pageable);
  }

  @Override
  public Page<Notification> findFailedNotificationsReadyForRetry(Instant currentTime, Pageable pageable) {
    return notificationRepository.findByStatusAndRetryCountLessThanAndCreatedDateAfter(
        NotificationStatus.FAILED,
        3,
        currentTime.minusSeconds(72 * 3600), // 72 hours ago
        pageable
    );
  }

  @Override
  public Page<Notification> findHighPriorityNotifications(Pageable pageable) {
    return notificationRepository.findByTypeIn(
        java.util.List.of(NotificationType.SECURITY_ALERT, NotificationType.SYSTEM_ALERT),
        pageable
    );
  }

  @Override
  public Page<Notification> findOverdueNotifications(Instant currentTime, Pageable pageable) {
    Instant highPriorityThreshold = currentTime.minusSeconds(5 * 60); // 5 minutes ago
    Instant normalThreshold = currentTime.minusSeconds(30 * 60); // 30 minutes ago

    return notificationRepository.findOverdueNotifications(highPriorityThreshold, normalThreshold, pageable);
  }

  @Override
  public Page<Notification> findExpiredNotifications(Instant expiryTime, Pageable pageable) {
    return notificationRepository.findByCreatedDateBefore(expiryTime, pageable);
  }

  @Override
  public long countByRecipientAndStatus(String recipient, NotificationStatus status) {
    return notificationRepository.countByRecipientAndStatus(recipient, status);
  }

  @Override
  public long countByTypeAndStatusAndCreatedDateBetween(NotificationType type, NotificationStatus status, Instant startTime, Instant endTime) {
    return notificationRepository.countByTypeAndStatusAndCreatedDateBetween(type, status, startTime, endTime);
  }

  @Override
  public long deleteNotificationsOlderThan(Instant cutoffDate) {
    return notificationRepository.deleteByCreatedDateBefore(cutoffDate);
  }

  @Override
  public Page<Notification> findByCreatedDateBetween(Instant startTime, Instant endTime, Pageable pageable) {
    return notificationRepository.findByCreatedDateBetween(startTime, endTime, pageable);
  }

  @Override
  public Optional<Notification> findMostRecentByRecipient(String recipient) {
    return notificationRepository.findFirstByRecipientOrderByCreatedDateDesc(recipient);
  }
}
