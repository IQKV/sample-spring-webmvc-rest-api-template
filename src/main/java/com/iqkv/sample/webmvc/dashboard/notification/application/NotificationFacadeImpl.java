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

import java.time.Instant;

import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.notification.infrastructure.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of NotificationFacade providing anti-corruption layer
 * for other bounded contexts to send notifications.
 */
@Component
@Transactional
public class NotificationFacadeImpl implements NotificationFacade {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationFacadeImpl.class);

  private final NotificationRepository notificationRepository;

  public NotificationFacadeImpl(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override
  public void sendNotification(Long userId, NotificationType type, NotificationChannel channel, String title, String message) {
    LOG.debug("Sending notification to user {} via {}: {}", userId, channel, title);

    Notification notification = new Notification();
    notification.setRecipient(userId.toString()); // Convert userId to string for recipient
    notification.setType(type);
    notification.setChannel(channel);
    notification.setSubject(title);
    notification.setContent(message);
    notification.setStatus(NotificationStatus.PENDING);

    notificationRepository.save(notification);

    // In a real implementation, this would trigger the actual notification sending
    // For now, we just mark it as sent
    notification.setStatus(NotificationStatus.SENT);
    notification.setSentAt(Instant.now());
    notificationRepository.save(notification);
  }

  @Override
  public void sendEmailNotification(Long userId, NotificationType type, String title, String message) {
    sendNotification(userId, type, NotificationChannel.EMAIL, title, message);
  }

  @Override
  public void sendSystemNotification(Long userId, NotificationType type, String title, String message) {
    sendNotification(userId, type, NotificationChannel.SYSTEM, title, message);
  }

  @Override
  public boolean areNotificationsEnabled(Long userId, NotificationChannel channel) {
    // In a real implementation, this would check user preferences
    // For now, we assume notifications are always enabled
    return true;
  }
}
