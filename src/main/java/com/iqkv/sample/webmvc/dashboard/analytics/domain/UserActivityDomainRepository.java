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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain repository interface for UserActivity aggregate.
 * <p>
 * This interface defines the contract for UserActivity persistence operations
 * from the domain perspective, without exposing infrastructure concerns.
 */
public interface UserActivityDomainRepository {

  /**
   * Saves a user activity.
   *
   * @param userActivity the user activity to save
   * @return the saved user activity
   */
  UserActivity save(UserActivity userActivity);

  /**
   * Finds a user activity by ID.
   *
   * @param id the activity ID
   * @return the user activity if found
   */
  Optional<UserActivity> findById(Long id);

  /**
   * Finds all activities for a specific user.
   *
   * @param userId   the user ID
   * @param pageable pagination information
   * @return page of user activities
   */
  Page<UserActivity> findByUserId(Long userId, Pageable pageable);

  /**
   * Finds activities by type for a specific user.
   *
   * @param userId       the user ID
   * @param activityType the activity type
   * @param pageable     pagination information
   * @return page of user activities
   */
  Page<UserActivity> findByUserIdAndActivityType(Long userId, ActivityType activityType, Pageable pageable);

  /**
   * Finds activities within a time range.
   *
   * @param userId    the user ID
   * @param startTime the start time
   * @param endTime   the end time
   * @param pageable  pagination information
   * @return page of user activities
   */
  Page<UserActivity> findByUserIdAndOccurredAtBetween(Long userId, Instant startTime, Instant endTime, Pageable pageable);

  /**
   * Finds security-sensitive activities for a user.
   *
   * @param userId   the user ID
   * @param pageable pagination information
   * @return page of security-sensitive activities
   */
  Page<UserActivity> findSecuritySensitiveActivities(Long userId, Pageable pageable);

  /**
   * Finds suspicious activities across all users.
   *
   * @param pageable pagination information
   * @return page of suspicious activities
   */
  Page<UserActivity> findSuspiciousActivities(Pageable pageable);

  /**
   * Finds activities by session ID.
   *
   * @param sessionId the session ID
   * @return list of activities for the session
   */
  List<UserActivity> findBySessionId(String sessionId);

  /**
   * Counts activities by type for a user within a time range.
   *
   * @param userId       the user ID
   * @param activityType the activity type
   * @param startTime    the start time
   * @param endTime      the end time
   * @return count of activities
   */
  long countByUserIdAndActivityTypeAndOccurredAtBetween(Long userId, ActivityType activityType, Instant startTime, Instant endTime);

  /**
   * Deletes old activities before a specific date.
   *
   * @param cutoffDate the cutoff date
   * @return number of deleted activities
   */
  long deleteActivitiesOlderThan(Instant cutoffDate);

  /**
   * Finds the most recent activity for a user.
   *
   * @param userId the user ID
   * @return the most recent activity if found
   */
  Optional<UserActivity> findMostRecentActivityByUserId(Long userId);

  /**
   * Finds activities by IP address for security analysis.
   *
   * @param ipAddress the IP address
   * @param pageable  pagination information
   * @return page of activities from the IP address
   */
  Page<UserActivity> findByIpAddress(String ipAddress, Pageable pageable);
}
