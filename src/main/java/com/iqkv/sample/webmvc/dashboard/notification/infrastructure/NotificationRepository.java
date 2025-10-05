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

import java.util.List;

import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
