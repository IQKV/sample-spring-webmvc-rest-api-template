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

package com.iqkv.sample.webmvc.dashboard.security.domain;

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
import java.util.HashMap;
import java.util.Map;

import com.iqkv.sample.webmvc.dashboard.shared.domain.AbstractAuditingEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * SecurityEvent aggregate root representing security-related events and incidents.
 */
@Entity
@Table(name = "security_event")
public class SecurityEvent extends AbstractAuditingEntity<Long> implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "securityEventSequenceGenerator")
  @SequenceGenerator(name = "securityEventSequenceGenerator")
  private Long id;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private SecurityEventType eventType;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "threat_level", nullable = false)
  private ThreatLevel threatLevel;

  @Column(name = "user_id")
  private Long userId;

  @Size(max = 100)
  @Column(name = "session_id", length = 100)
  private String sessionId;

  @Size(max = 45)
  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Size(max = 500)
  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @NotNull
  @Size(max = 500)
  @Column(name = "description", length = 500, nullable = false)
  private String description;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "event_data", columnDefinition = "jsonb")
  private Map<String, Object> eventData;

  @Column(name = "resolved", nullable = false)
  private boolean resolved = false;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Size(max = 100)
  @Column(name = "resolved_by", length = 100)
  private String resolvedBy;

  // Domain methods

  /**
   * Creates a failed login security event.
   */
  public static SecurityEvent failedLogin(String login, String ipAddress, String userAgent) {
    SecurityEvent event = new SecurityEvent();
    event.eventType = SecurityEventType.FAILED_LOGIN;
    event.threatLevel = ThreatLevel.LOW;
    event.ipAddress = ipAddress;
    event.userAgent = userAgent;
    event.description = "Failed login attempt for user: " + login;
    event.occurredAt = Instant.now();
    event.eventData = Map.of("login", login, "attempt_time", Instant.now().toString());
    return event;
  }

  /**
   * Creates a suspicious activity security event.
   */
  public static SecurityEvent suspiciousActivity(Long userId, String sessionId, String description, Map<String, Object> data) {
    SecurityEvent event = new SecurityEvent();
    event.eventType = SecurityEventType.SUSPICIOUS_ACTIVITY;
    event.threatLevel = ThreatLevel.MEDIUM;
    event.userId = userId;
    event.sessionId = sessionId;
    event.description = description;
    event.occurredAt = Instant.now();
    event.eventData = data;
    return event;
  }

  /**
   * Creates a privilege escalation security event.
   */
  public static SecurityEvent privilegeEscalation(Long userId, String sessionId, String details) {
    SecurityEvent event = new SecurityEvent();
    event.eventType = SecurityEventType.PRIVILEGE_ESCALATION;
    event.threatLevel = ThreatLevel.HIGH;
    event.userId = userId;
    event.sessionId = sessionId;
    event.description = "Privilege escalation detected: " + details;
    event.occurredAt = Instant.now();
    return event;
  }

  /**
   * Resolves the security event.
   *
   * @param resolvedBy who resolved the event
   */
  public void resolve(String resolvedBy) {
    if (this.resolved) {
      throw new IllegalStateException("Security event is already resolved");
    }
    this.resolved = true;
    this.resolvedAt = Instant.now();
    this.resolvedBy = resolvedBy;
  }

  /**
   * Escalates the threat level of the event.
   *
   * @param newThreatLevel the new threat level
   */
  public void escalateThreatLevel(ThreatLevel newThreatLevel) {
    if (newThreatLevel.ordinal() <= this.threatLevel.ordinal()) {
      throw new IllegalArgumentException("New threat level must be higher than current level");
    }
    this.threatLevel = newThreatLevel;
  }

  /**
   * Checks if the event requires immediate attention.
   */
  public boolean requiresImmediateAttention() {
    return threatLevel == ThreatLevel.CRITICAL || threatLevel == ThreatLevel.HIGH;
  }

  /**
   * Gets the age of the event in minutes.
   */
  public long getAgeInMinutes() {
    return java.time.Duration.between(occurredAt, Instant.now()).toMinutes();
  }

  // Getters and setters

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SecurityEventType getEventType() {
    return eventType;
  }

  public void setEventType(SecurityEventType eventType) {
    this.eventType = eventType;
  }

  public ThreatLevel getThreatLevel() {
    return threatLevel;
  }

  public void setThreatLevel(ThreatLevel threatLevel) {
    this.threatLevel = threatLevel;
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

  public Map<String, Object> getEventData() {
    return eventData;
  }

  public void setEventData(Map<String, Object> eventData) {
    this.eventData = eventData;
  }

  public boolean isResolved() {
    return resolved;
  }

  public void setResolved(boolean resolved) {
    this.resolved = resolved;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(Instant resolvedAt) {
    this.resolvedAt = resolvedAt;
  }

  public String getResolvedBy() {
    return resolvedBy;
  }

  public void setResolvedBy(String resolvedBy) {
    this.resolvedBy = resolvedBy;
  }

  /**
   * Gets the metadata (alias for eventData for backward compatibility).
   *
   * @return the event metadata
   */
  public Map<String, Object> getMetadata() {
    return eventData;
  }

  /**
   * Gets a metadata value by key.
   *
   * @param key the metadata key
   * @return the metadata value or null if not found
   */
  public Object getMetadataValue(String key) {
    return eventData != null ? eventData.get(key) : null;
  }

  /**
   * Gets the login from event data (for backward compatibility).
   *
   * @return the login from event data or null if not found
   */
  public String getLogin() {
    return eventData != null ? (String) eventData.get("login") : null;
  }

  /**
   * Sets the login in event data.
   *
   * @param login the login to set
   */
  public void setLogin(String login) {
    if (eventData == null) {
      eventData = new HashMap<>();
    }
    eventData.put("login", login);
  }

  /**
   * Gets the timestamp (alias for occurredAt for backward compatibility).
   *
   * @return the timestamp when event occurred
   */
  public Instant getTimestamp() {
    return occurredAt;
  }

  /**
   * Sets the metadata (alias for setEventData for backward compatibility).
   *
   * @param metadata the metadata to set
   */
  public void setMetadata(Map<String, Object> metadata) {
    this.eventData = metadata;
  }

  /**
   * Checks if the event occurred recently (within last hour).
   *
   * @return true if event is recent
   */
  public boolean isRecent() {
    return getAgeInMinutes() <= 60;
  }

  /**
   * Checks if the event is suspicious based on type and threat level.
   *
   * @return true if event is suspicious
   */
  public boolean isSuspicious() {
    return threatLevel == ThreatLevel.HIGH || threatLevel == ThreatLevel.CRITICAL ||
           eventType == SecurityEventType.SUSPICIOUS_ACTIVITY ||
           eventType == SecurityEventType.BRUTE_FORCE_ATTACK;
  }

  /**
   * Checks if the event requires user context.
   *
   * @return true if event requires user context
   */
  public boolean requiresUserContext() {
    return eventType != SecurityEventType.SYSTEM_EVENT && 
           eventType != SecurityEventType.IP_BLOCKED;
  }

  /**
   * Gets a summary of the security event.
   *
   * @return event summary
   */
  public String getSummary() {
    String login = getLogin();
    if (login != null) {
      return eventType + " for " + login + " at " + occurredAt;
    }
    return eventType + " at " + occurredAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SecurityEvent)) {
      return false;
    }
    return id != null && id.equals(((SecurityEvent) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "SecurityEvent{" +
           "id=" + id +
           ", eventType=" + eventType +
           ", threatLevel=" + threatLevel +
           ", userId=" + userId +
           ", description='" + description + '\'' +
           ", occurredAt=" + occurredAt +
           ", resolved=" + resolved +
           '}';
  }
}
