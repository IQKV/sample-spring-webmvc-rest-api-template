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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain repository interface for SecurityEvent aggregate.
 * <p>
 * This interface defines the contract for SecurityEvent persistence operations
 * from the domain perspective, without exposing infrastructure concerns.
 */
public interface SecurityEventDomainRepository {

  /**
   * Saves a security event.
   *
   * @param securityEvent the security event to save
   * @return the saved security event
   */
  SecurityEvent save(SecurityEvent securityEvent);

  /**
   * Finds a security event by ID.
   *
   * @param id the event ID
   * @return the security event if found
   */
  Optional<SecurityEvent> findById(Long id);

  /**
   * Finds all security events for a specific user.
   *
   * @param userId   the user ID
   * @param pageable pagination information
   * @return page of security events
   */
  Page<SecurityEvent> findByUserId(Long userId, Pageable pageable);

  /**
   * Finds security events by type.
   *
   * @param eventType the event type
   * @param pageable  pagination information
   * @return page of security events
   */
  Page<SecurityEvent> findByEventType(SecurityEventType eventType, Pageable pageable);

  /**
   * Finds security events by threat level.
   *
   * @param threatLevel the threat level
   * @param pageable    pagination information
   * @return page of security events
   */
  Page<SecurityEvent> findByThreatLevel(ThreatLevel threatLevel, Pageable pageable);

  /**
   * Finds unresolved security events.
   *
   * @param pageable pagination information
   * @return page of unresolved security events
   */
  Page<SecurityEvent> findUnresolvedEvents(Pageable pageable);

  /**
   * Finds security events requiring immediate attention.
   *
   * @param pageable pagination information
   * @return page of high-priority security events
   */
  Page<SecurityEvent> findEventsRequiringImmediateAttention(Pageable pageable);

  /**
   * Finds security events by IP address.
   *
   * @param ipAddress the IP address
   * @param pageable  pagination information
   * @return page of security events from the IP address
   */
  Page<SecurityEvent> findByIpAddress(String ipAddress, Pageable pageable);

  /**
   * Finds security events by session ID.
   *
   * @param sessionId the session ID
   * @return list of security events for the session
   */
  List<SecurityEvent> findBySessionId(String sessionId);

  /**
   * Finds security events within a time range.
   *
   * @param startTime the start time
   * @param endTime   the end time
   * @param pageable  pagination information
   * @return page of security events
   */
  Page<SecurityEvent> findByOccurredAtBetween(Instant startTime, Instant endTime, Pageable pageable);

  /**
   * Finds recent security events for a user.
   *
   * @param userId   the user ID
   * @param since    the time threshold
   * @param pageable pagination information
   * @return page of recent security events
   */
  Page<SecurityEvent> findRecentEventsByUserId(Long userId, Instant since, Pageable pageable);

  /**
   * Counts security events by type and threat level within a time range.
   *
   * @param eventType   the event type
   * @param threatLevel the threat level
   * @param startTime   the start time
   * @param endTime     the end time
   * @return count of security events
   */
  long countByEventTypeAndThreatLevelAndOccurredAtBetween(SecurityEventType eventType, ThreatLevel threatLevel, Instant startTime, Instant endTime);

  /**
   * Counts failed login attempts for a user within a time range.
   *
   * @param userId    the user ID (can be null for unknown users)
   * @param ipAddress the IP address
   * @param startTime the start time
   * @param endTime   the end time
   * @return count of failed login attempts
   */
  long countFailedLoginAttempts(Long userId, String ipAddress, Instant startTime, Instant endTime);

  /**
   * Finds security events by user and event type within a time range.
   *
   * @param userId    the user ID
   * @param eventType the event type
   * @param startTime the start time
   * @param endTime   the end time
   * @param pageable  pagination information
   * @return page of security events
   */
  Page<SecurityEvent> findByUserIdAndEventTypeAndOccurredAtBetween(Long userId, SecurityEventType eventType, Instant startTime, Instant endTime, Pageable pageable);

  /**
   * Deletes old security events before a specific date.
   *
   * @param cutoffDate the cutoff date
   * @return number of deleted security events
   */
  long deleteEventsOlderThan(Instant cutoffDate);

  /**
   * Finds the most recent security event for a user.
   *
   * @param userId the user ID
   * @return the most recent security event if found
   */
  Optional<SecurityEvent> findMostRecentEventByUserId(Long userId);

  /**
   * Finds security events that need escalation based on age and threat level.
   *
   * @param maxAge   the maximum age for unresolved events
   * @param pageable pagination information
   * @return page of events needing escalation
   */
  Page<SecurityEvent> findEventsNeedingEscalation(Instant maxAge, Pageable pageable);
}
