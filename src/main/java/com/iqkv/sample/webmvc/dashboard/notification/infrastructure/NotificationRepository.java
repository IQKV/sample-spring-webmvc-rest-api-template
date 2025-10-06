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
import java.util.List;
import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Notification aggregate.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /**
   * Finds all notifications by status.
   *
   * @param status the notification status
   * @return list of notifications
   */
  List<Notification> findByStatus(NotificationStatus status);

  /**
   * Finds all notifications by recipient.
   *
   * @param recipient the recipient
   * @return list of notifications
   */
  List<Notification> findByRecipient(String recipient);

  /**
   * Finds all failed notifications that can be retried.
   *
   * @return list of notifications
   */
  List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

  // Additional methods for domain repository adapter

  Page<Notification> findByRecipient(String recipient, Pageable pageable);

  Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

  Page<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, Pageable pageable);

  Page<Notification> findByChannelAndStatus(NotificationChannel channel, NotificationStatus status, Pageable pageable);

  Page<Notification> findByStatusAndRetryCountLessThanAndCreatedDateAfter(NotificationStatus status, Integer maxRetries, Instant createdAfter, Pageable pageable);

  Page<Notification> findByTypeIn(List<NotificationType> types, Pageable pageable);

  @Query("SELECT n FROM Notification n WHERE " +
         "(n.type IN ('SECURITY_ALERT', 'SYSTEM_ALERT') AND n.createdDate < :highPriorityThreshold) OR " +
         "(n.type NOT IN ('SECURITY_ALERT', 'SYSTEM_ALERT') AND n.createdDate < :normalThreshold)")
  Page<Notification> findOverdueNotifications(@Param("highPriorityThreshold") Instant highPriorityThreshold,
                                              @Param("normalThreshold") Instant normalThreshold,
                                              Pageable pageable);

  Page<Notification> findByCreatedDateBefore(Instant cutoffDate, Pageable pageable);

  long countByRecipientAndStatus(String recipient, NotificationStatus status);

  long countByTypeAndStatusAndCreatedDateBetween(NotificationType type, NotificationStatus status, Instant startTime, Instant endTime);

  long deleteByCreatedDateBefore(Instant cutoffDate);

  Page<Notification> findByCreatedDateBetween(Instant startTime, Instant endTime, Pageable pageable);

  Optional<Notification> findFirstByRecipientOrderByCreatedDateDesc(String recipient);
}
