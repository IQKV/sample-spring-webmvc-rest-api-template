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

package com.iqkv.sample.webmvc.dashboard.analytics.domain;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import com.iqkv.sample.webmvc.dashboard.shared.domain.AbstractAuditingEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * UserActivity aggregate root representing user behavior tracking and analytics.
 * <p>
 * This entity encapsulates user activity data with business logic for
 * analytics, security monitoring, and user behavior analysis within
 * the Analytics bounded context.
 */
@Entity
@Table(name = "user_activity")
public class UserActivity extends AbstractAuditingEntity<Long> implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final Set<ActivityType> HIGH_VALUE_ACTIVITIES = Set.of(
      ActivityType.LOGIN, ActivityType.PROFILE_UPDATE, ActivityType.PASSWORD_CHANGE
  );

  private static final Set<ActivityType> SECURITY_ACTIVITIES = Set.of(
      ActivityType.LOGIN, ActivityType.LOGOUT, ActivityType.FAILED_LOGIN,
      ActivityType.PASSWORD_CHANGE
  );

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activitySequenceGenerator")
  @SequenceGenerator(name = "activitySequenceGenerator")
  private Long id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Size(max = 100)
  @Column(name = "session_id", length = 100)
  private String sessionId;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false)
  private ActivityType activityType;

  @Size(max = 200)
  @Column(name = "description", length = 200)
  private String description;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  // Domain methods

  /**
   * Records a user login activity.
   */
  public static UserActivity recordLogin(Long userId, String sessionId, String ipAddress, String userAgent) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.sessionId = sessionId;
    activity.activityType = ActivityType.LOGIN;
    activity.description = "User logged in";
    activity.occurredAt = Instant.now();
    activity.ipAddress = ipAddress;
    activity.userAgent = userAgent;
    return activity;
  }

  /**
   * Records a user logout activity.
   */
  public static UserActivity recordLogout(Long userId, String sessionId) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.sessionId = sessionId;
    activity.activityType = ActivityType.LOGOUT;
    activity.description = "User logged out";
    activity.occurredAt = Instant.now();
    return activity;
  }

  /**
   * Records a profile update activity.
   */
  public static UserActivity recordProfileUpdate(Long userId, String sessionId, Map<String, Object> changes) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.sessionId = sessionId;
    activity.activityType = ActivityType.PROFILE_UPDATE;
    activity.description = "User updated profile";
    activity.occurredAt = Instant.now();
    activity.metadata = changes;
    return activity;
  }

  /**
   * Records a password change activity.
   */
  public static UserActivity recordPasswordChange(Long userId, String sessionId) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.sessionId = sessionId;
    activity.activityType = ActivityType.PASSWORD_CHANGE;
    activity.description = "User changed password";
    activity.occurredAt = Instant.now();
    return activity;
  }

  /**
   * Checks if this activity is a security-sensitive action.
   */
  public boolean isSecuritySensitive() {
    return SECURITY_ACTIVITIES.contains(activityType);
  }

  /**
   * Checks if this activity is considered high-value for analytics.
   */
  public boolean isHighValueActivity() {
    return HIGH_VALUE_ACTIVITIES.contains(activityType);
  }

  /**
   * Gets the duration since the activity occurred.
   */
  public long getMinutesSinceOccurred() {
    return Duration.between(occurredAt, Instant.now()).toMinutes();
  }

  /**
   * Gets the hour of day when this activity occurred (0-23).
   */
  public int getHourOfDay() {
    return LocalDateTime.ofInstant(occurredAt, ZoneOffset.UTC).getHour();
  }

  /**
   * Gets the day of week when this activity occurred (1=Monday, 7=Sunday).
   */
  public int getDayOfWeek() {
    return LocalDateTime.ofInstant(occurredAt, ZoneOffset.UTC).getDayOfWeek().getValue();
  }

  /**
   * Checks if this activity occurred during business hours (9 AM - 5 PM UTC).
   */
  public boolean isBusinessHours() {
    int hour = getHourOfDay();
    return hour >= 9 && hour < 17;
  }

  /**
   * Checks if this activity occurred during weekend.
   */
  public boolean isWeekend() {
    int dayOfWeek = getDayOfWeek();
    return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
  }

  /**
   * Gets the activity score for analytics (higher = more important).
   */
  public int getActivityScore() {
    int baseScore = switch (activityType) {
      case LOGIN -> 10;
      case LOGOUT -> 5;
      case PROFILE_UPDATE -> 8;
      case PASSWORD_CHANGE -> 15;
      case FAILED_LOGIN -> 20; // High score for security monitoring
      case PAGE_VIEW -> 1;
      case FEATURE_USE -> 3;
      default -> 5; // Default score for other activity types
    };

    // Boost score for unusual timing
    if (!isBusinessHours()) {
      baseScore += 2;
    }
    if (isWeekend()) {
      baseScore += 3;
    }

    return baseScore;
  }

  /**
   * Records a failed login attempt.
   */
  public static UserActivity recordFailedLogin(Long userId, String ipAddress, String userAgent, String reason) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.activityType = ActivityType.FAILED_LOGIN;
    activity.description = "Failed login attempt: " + reason;
    activity.occurredAt = Instant.now();
    activity.ipAddress = ipAddress;
    activity.userAgent = userAgent;
    activity.metadata = Map.of("failure_reason", reason, "timestamp", Instant.now().toString());
    return activity;
  }

  /**
   * Records a page view activity.
   */
  public static UserActivity recordPageView(Long userId, String sessionId, String page, String referrer) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.sessionId = sessionId;
    activity.activityType = ActivityType.PAGE_VIEW;
    activity.description = "Viewed page: " + page;
    activity.occurredAt = Instant.now();
    activity.metadata = Map.of(
        "page", page,
        "referrer", referrer != null ? referrer : "",
        "timestamp", Instant.now().toString()
    );
    return activity;
  }

  /**
   * Records a feature usage activity.
   */
  public static UserActivity recordFeatureUse(Long userId, String sessionId, String feature, Map<String, Object> context) {
    UserActivity activity = new UserActivity();
    activity.userId = userId;
    activity.sessionId = sessionId;
    activity.activityType = ActivityType.FEATURE_USE;
    activity.description = "Used feature: " + feature;
    activity.occurredAt = Instant.now();
    activity.metadata = context != null ? context : Map.of();
    return activity;
  }

  /**
   * Checks if this activity indicates suspicious behavior.
   */
  public boolean isSuspicious() {
    // Multiple failed logins are suspicious
    if (activityType == ActivityType.FAILED_LOGIN) {
      return true;
    }

    // Login from unusual location/time might be suspicious
    if (activityType == ActivityType.LOGIN && (!isBusinessHours() || isWeekend())) {
      return true;
    }

    return false;
  }

  /**
   * Gets user engagement level based on activity type.
   */
  public EngagementLevel getEngagementLevel() {
    return switch (activityType) {
      case LOGIN, LOGOUT -> EngagementLevel.SESSION;
      case PROFILE_UPDATE, PASSWORD_CHANGE -> EngagementLevel.HIGH;
      case FEATURE_USE -> EngagementLevel.MEDIUM;
      case PAGE_VIEW -> EngagementLevel.LOW;
      case FAILED_LOGIN -> EngagementLevel.NONE;
      default -> EngagementLevel.LOW; // Default engagement level for other activity types
    };
  }

  /**
   * Creates activity analytics summary.
   */
  public ActivityAnalytics getAnalytics() {
    return new ActivityAnalytics(
        activityType,
        getActivityScore(),
        getEngagementLevel(),
        isSecuritySensitive(),
        isHighValueActivity(),
        isSuspicious(),
        isBusinessHours(),
        isWeekend(),
        getHourOfDay(),
        getDayOfWeek()
    );
  }

  /**
   * Validates activity data according to business rules.
   */
  public void validateActivity() {
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    if (activityType == null) {
      throw new IllegalArgumentException("Activity type cannot be null");
    }

    if (occurredAt == null) {
      throw new IllegalArgumentException("Occurred time cannot be null");
    }

    // Future activities are not allowed
    if (occurredAt.isAfter(Instant.now())) {
      throw new IllegalArgumentException("Activity cannot occur in the future");
    }

    // Activities older than 1 year are suspicious
    if (occurredAt.isBefore(Instant.now().minus(365, java.time.temporal.ChronoUnit.DAYS))) {
      throw new IllegalArgumentException("Activity is too old to be valid");
    }
  }

  /**
   * Enum for user engagement levels.
   */
  public enum EngagementLevel {
    NONE, LOW, MEDIUM, HIGH, SESSION
  }

  /**
   * Record class for activity analytics data.
   */
  public record ActivityAnalytics(
      ActivityType activityType,
      int activityScore,
      EngagementLevel engagementLevel,
      boolean isSecuritySensitive,
      boolean isHighValue,
      boolean isSuspicious,
      boolean isBusinessHours,
      boolean isWeekend,
      int hourOfDay,
      int dayOfWeek
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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public ActivityType getActivityType() {
    return activityType;
  }

  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(Instant occurredAt) {
    this.occurredAt = occurredAt;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   * Gets the timestamp (alias for occurredAt for backward compatibility).
   *
   * @return the timestamp when activity occurred
   */
  public Instant getTimestamp() {
    return occurredAt;
  }

  /**
   * Checks if the activity occurred recently (within last hour).
   *
   * @return true if activity is recent
   */
  public boolean isRecent() {
    return getMinutesSinceOccurred() <= 60;
  }

  /**
   * Checks if the activity occurred today.
   *
   * @return true if activity occurred today
   */
  public boolean isToday() {
    LocalDate activityDate = LocalDateTime.ofInstant(occurredAt, ZoneOffset.UTC).toLocalDate();
    return activityDate.equals(LocalDate.now());
  }

  /**
   * Gets a metadata value by key.
   *
   * @param key the metadata key
   * @return the metadata value or null if not found
   */
  public Object getMetadataValue(String key) {
    return metadata != null ? metadata.get(key) : null;
  }

  /**
   * Checks if metadata contains a specific key.
   *
   * @param key the metadata key
   * @return true if metadata contains the key
   */
  public boolean hasMetadata(String key) {
    return metadata != null && metadata.containsKey(key);
  }

  /**
   * Creates a builder for UserActivity.
   *
   * @return a new UserActivity builder
   */
  public static UserActivityBuilder builder() {
    return new UserActivityBuilder();
  }

  /**
   * Builder class for UserActivity.
   */
  public static class UserActivityBuilder {
    private UserActivity activity = new UserActivity();

    public UserActivityBuilder userId(Long userId) {
      activity.userId = userId;
      return this;
    }

    public UserActivityBuilder activityType(ActivityType activityType) {
      activity.activityType = activityType;
      return this;
    }

    public UserActivityBuilder description(String description) {
      activity.description = description;
      return this;
    }

    public UserActivityBuilder occurredAt(Instant occurredAt) {
      activity.occurredAt = occurredAt;
      return this;
    }

    public UserActivityBuilder timestamp(LocalDateTime timestamp) {
      activity.occurredAt = timestamp.atZone(ZoneOffset.UTC).toInstant();
      return this;
    }

    public UserActivityBuilder metadata(Map<String, Object> metadata) {
      activity.metadata = metadata;
      return this;
    }

    public UserActivity build() {
      return activity;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserActivity)) {
      return false;
    }
    return id != null && id.equals(((UserActivity) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "UserActivity{" +
           "id=" + id +
           ", userId=" + userId +
           ", activityType=" + activityType +
           ", description='" + description + '\'' +
           ", occurredAt=" + occurredAt +
           '}';
  }
}
