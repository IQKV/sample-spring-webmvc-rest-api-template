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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.iqkv.sample.webmvc.dashboard.shared.domain.AbstractAuditingEntity;

/**
 * Notification aggregate root representing a notification in the system.
 * <p>
 * This entity encapsulates notification business logic including delivery rules,
 * retry mechanisms, and priority handling within the Notification bounded context.
 */
@Entity
@Table(name = "notification")
public class Notification extends AbstractAuditingEntity<Long> implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final int RETRY_DELAY_MINUTES = 15;
  private static final int NOTIFICATION_EXPIRY_HOURS = 72;

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
    return this.status == NotificationStatus.FAILED &&
           this.retryCount < MAX_RETRY_ATTEMPTS &&
           !isExpired();
  }

  /**
   * Checks if the notification has expired and should not be sent.
   *
   * @return true if notification has expired
   */
  public boolean isExpired() {
    if (getCreatedDate() == null) {
      return false;
    }

    Instant expiryTime = getCreatedDate().plus(NOTIFICATION_EXPIRY_HOURS, ChronoUnit.HOURS);
    return Instant.now().isAfter(expiryTime);
  }

  /**
   * Calculates the next retry time based on exponential backoff.
   *
   * @return the next retry time
   */
  public Instant getNextRetryTime() {
    if (this.status != NotificationStatus.FAILED) {
      return null;
    }

    // Exponential backoff: 15min, 30min, 60min
    long delayMinutes = RETRY_DELAY_MINUTES * (long) Math.pow(2, retryCount - 1);
    return Instant.now().plus(delayMinutes, ChronoUnit.MINUTES);
  }

  /**
   * Checks if the notification is ready for retry.
   *
   * @return true if ready for retry
   */
  public boolean isReadyForRetry() {
    if (!canRetry()) {
      return false;
    }

    Instant nextRetryTime = getNextRetryTime();
    return nextRetryTime != null && Instant.now().isAfter(nextRetryTime);
  }

  /**
   * Gets the priority level based on notification type.
   *
   * @return priority level (1=highest, 5=lowest)
   */
  public int getPriority() {
    return switch (type) {
      case SECURITY_ALERT -> 1;
      case SYSTEM_ALERT -> 2;
      case USER_ACTION -> 3;
      case REMINDER -> 4;
      case MARKETING -> 5;
      default -> 3; // Default priority for other notification types
    };
  }

  /**
   * Checks if this is a high-priority notification.
   *
   * @return true if high priority
   */
  public boolean isHighPriority() {
    return getPriority() <= 2;
  }

  /**
   * Gets the age of the notification in minutes.
   *
   * @return age in minutes
   */
  public long getAgeInMinutes() {
    if (getCreatedDate() == null) {
      return 0;
    }
    return Duration.between(getCreatedDate(), Instant.now()).toMinutes();
  }

  /**
   * Checks if the notification delivery is overdue.
   *
   * @return true if overdue
   */
  public boolean isOverdue() {
    if (status == NotificationStatus.SENT || getCreatedDate() == null) {
      return false;
    }

    // High priority: overdue after 5 minutes, normal: after 30 minutes
    long overdueThreshold = isHighPriority() ? 5 : 30;
    return getAgeInMinutes() > overdueThreshold;
  }

  /**
   * Creates a notification for user registration.
   *
   * @param recipient     the recipient email
   * @param activationUrl the activation URL
   * @return a new notification instance
   */
  public static Notification createUserRegistration(String recipient, String activationUrl) {
    Notification notification = new Notification();
    notification.recipient = recipient;
    notification.subject = "Welcome! Please activate your account";
    notification.content = "Please click the following link to activate your account: " + activationUrl;
    notification.type = NotificationType.USER_ACTION;
    notification.channel = NotificationChannel.EMAIL;
    notification.status = NotificationStatus.PENDING;
    return notification;
  }

  /**
   * Creates a notification for password reset.
   *
   * @param recipient the recipient email
   * @param resetUrl  the password reset URL
   * @return a new notification instance
   */
  public static Notification createPasswordReset(String recipient, String resetUrl) {
    Notification notification = new Notification();
    notification.recipient = recipient;
    notification.subject = "Password Reset Request";
    notification.content = "Click the following link to reset your password: " + resetUrl;
    notification.type = NotificationType.SECURITY_ALERT;
    notification.channel = NotificationChannel.EMAIL;
    notification.status = NotificationStatus.PENDING;
    return notification;
  }

  /**
   * Creates a security alert notification.
   *
   * @param recipient    the recipient
   * @param alertMessage the alert message
   * @param channel      the notification channel
   * @return a new notification instance
   */
  public static Notification createSecurityAlert(String recipient, String alertMessage, NotificationChannel channel) {
    Notification notification = new Notification();
    notification.recipient = recipient;
    notification.subject = "Security Alert";
    notification.content = alertMessage;
    notification.type = NotificationType.SECURITY_ALERT;
    notification.channel = channel;
    notification.status = NotificationStatus.PENDING;
    return notification;
  }

  /**
   * Creates a system maintenance notification.
   *
   * @param recipient          the recipient
   * @param maintenanceDetails the maintenance details
   * @return a new notification instance
   */
  public static Notification createSystemMaintenance(String recipient, String maintenanceDetails) {
    Notification notification = new Notification();
    notification.recipient = recipient;
    notification.subject = "Scheduled System Maintenance";
    notification.content = maintenanceDetails;
    notification.type = NotificationType.SYSTEM_ALERT;
    notification.channel = NotificationChannel.EMAIL;
    notification.status = NotificationStatus.PENDING;
    return notification;
  }

  /**
   * Validates notification content according to business rules.
   *
   * @throws IllegalArgumentException if validation fails
   */
  public void validateContent() {
    if (recipient == null || recipient.trim().isEmpty()) {
      throw new IllegalArgumentException("Recipient cannot be null or empty");
    }

    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }

    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("Content cannot be null or empty");
    }

    // Channel-specific validation
    if (channel == NotificationChannel.SMS && content.length() > 160) {
      throw new IllegalArgumentException("SMS content cannot exceed 160 characters");
    }

    if (channel == NotificationChannel.EMAIL && !recipient.contains("@")) {
      throw new IllegalArgumentException("Email recipient must be a valid email address");
    }
  }

  /**
   * Gets delivery statistics for this notification.
   *
   * @return delivery statistics
   */
  public NotificationDeliveryStats getDeliveryStats() {
    return new NotificationDeliveryStats(
        getAgeInMinutes(),
        retryCount,
        status,
        isOverdue(),
        isExpired()
    );
  }

  /**
   * Record class for notification delivery statistics.
   */
  public record NotificationDeliveryStats(
      long ageInMinutes,
      int retryCount,
      NotificationStatus status,
      boolean isOverdue,
      boolean isExpired
  ) {
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
