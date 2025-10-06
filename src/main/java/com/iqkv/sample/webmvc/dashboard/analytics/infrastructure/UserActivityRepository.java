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

package com.iqkv.sample.webmvc.dashboard.analytics.infrastructure;

import java.time.Instant;
import java.util.List;

import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for UserActivity aggregate.
 */
@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

  /**
   * Finds all activities for a specific user.
   *
   * @param userId   the user ID
   * @param pageable pagination information
   * @return page of user activities
   */
  Page<UserActivity> findByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

  /**
   * Finds activities by type within a date range.
   *
   * @param activityType the activity type
   * @param startDate    the start date
   * @param endDate      the end date
   * @return list of activities
   */
  List<UserActivity> findByActivityTypeAndOccurredAtBetween(
      ActivityType activityType,
      Instant startDate,
      Instant endDate
  );

  /**
   * Finds security-sensitive activities for a user.
   *
   * @param userId        the user ID
   * @param activityTypes the security-sensitive activity types
   * @param pageable      pagination information
   * @return page of security activities
   */
  Page<UserActivity> findByUserIdAndActivityTypeInOrderByOccurredAtDesc(
      Long userId,
      List<ActivityType> activityTypes,
      Pageable pageable
  );

  /**
   * Counts activities by type within a date range.
   *
   * @param activityType the activity type
   * @param startDate    the start date
   * @param endDate      the end date
   * @return count of activities
   */
  @Query("SELECT COUNT(a) FROM UserActivity a WHERE a.activityType = :activityType AND a.occurredAt BETWEEN :startDate AND :endDate")
  long countByActivityTypeAndDateRange(
      @Param("activityType") ActivityType activityType,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate
  );

  /**
   * Finds recent activities for dashboard display.
   *
   * @param limit the maximum number of activities to return
   * @return list of recent activities
   */
  @Query("SELECT a FROM UserActivity a ORDER BY a.occurredAt DESC LIMIT :limit")
  List<UserActivity> findRecentActivities(@Param("limit") int limit);

  // Additional methods for domain repository adapter

  Page<UserActivity> findByUserId(Long userId, Pageable pageable);

  Page<UserActivity> findByUserIdAndActivityType(Long userId, ActivityType activityType, Pageable pageable);

  Page<UserActivity> findByUserIdAndOccurredAtBetween(Long userId, Instant startTime, Instant endTime, Pageable pageable);

  Page<UserActivity> findByUserIdAndActivityTypeIn(Long userId, List<ActivityType> activityTypes, Pageable pageable);

  Page<UserActivity> findByActivityType(ActivityType activityType, Pageable pageable);

  List<UserActivity> findBySessionId(String sessionId);

  long countByUserIdAndActivityTypeAndOccurredAtBetween(Long userId, ActivityType activityType, Instant startTime, Instant endTime);

  long deleteByOccurredAtBefore(Instant cutoffDate);

  java.util.Optional<UserActivity> findFirstByUserIdOrderByOccurredAtDesc(Long userId);

  Page<UserActivity> findByIpAddress(String ipAddress, Pageable pageable);
}
