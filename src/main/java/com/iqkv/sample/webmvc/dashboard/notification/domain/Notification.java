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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.iqkv.sample.webmvc.dashboard.shared.domain.AbstractAuditingEntity;

/**
 * Notification aggregate root representing a notification in the system.
 */
@Entity
@Table(name = "notification")
public class Notification extends AbstractAuditingEntity<Long> implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificationSequenceGenerator")
  @SequenceGenerator(name = "notificationSequenceGenerator")
  private Long id;

  @NotNull
  @Size(max = 100)
  @Column(name = "recipient", length = 100, nullable = false)
  private String recipient;

  @NotNull
  @Size(max = 200)
  @Column(name = "subject", length = 200, nullable = false)
  private String subject;

  @NotNull
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private NotificationType type;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "channel", nullable = false)
  private NotificationChannel channel;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private NotificationStatus status = NotificationStatus.PENDING;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "retry_count")
  private Integer retryCount = 0;

  // Domain methods

  /**
   * Marks the notification as sent.
   */
  public void markAsSent() {
    if (this.status == NotificationStatus.SENT) {
      throw new IllegalStateException("Notification is already sent");
    }
    this.status = NotificationStatus.SENT;
    this.sentAt = Instant.now();
    this.errorMessage = null;
  }

  /**
   * Marks the notification as failed.
   *
   * @param errorMessage the error message
   */
  public void markAsFailed(String errorMessage) {
    this.status = NotificationStatus.FAILED;
    this.errorMessage = errorMessage;
    this.retryCount++;
  }

  /**
   * Retries sending the notification.
   *
   * @throws IllegalStateException if notification cannot be retried
   */
  public void retry() {
    if (this.status != NotificationStatus.FAILED) {
      throw new IllegalStateException("Only failed notifications can be retried");
    }
    if (this.retryCount >= 3) {
      throw new IllegalStateException("Maximum retry attempts exceeded");
    }
    this.status = NotificationStatus.PENDING;
    this.errorMessage = null;
  }

  /**
   * Checks if the notification can be retried.
   *
   * @return true if notification can be retried
   */
  public boolean canRetry() {
    return this.status == NotificationStatus.FAILED && this.retryCount < 3;
  }

  // Getters and setters

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public NotificationChannel getChannel() {
    return channel;
  }

  public void setChannel(NotificationChannel channel) {
    this.channel = channel;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public void setStatus(NotificationStatus status) {
    this.status = status;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public void setSentAt(Instant sentAt) {
    this.sentAt = sentAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Integer getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(Integer retryCount) {
    this.retryCount = retryCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Notification)) {
      return false;
    }
    return id != null && id.equals(((Notification) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Notification{" +
           "id=" + id +
           ", recipient='" + recipient + '\'' +
           ", subject='" + subject + '\'' +
           ", type=" + type +
           ", channel=" + channel +
           ", status=" + status +
           ", sentAt=" + sentAt +
           '}';
  }
}
