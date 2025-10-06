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

package com.iqkv.sample.webmvc.dashboard.analytics.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;

/**
 * Service client interface for Analytics operations.
 * <p>
 * This interface provides a clean abstraction for accessing analytics
 * functionality that can be implemented for both monolithic (local) and
 * microservices (remote) deployment scenarios.
 */
public interface AnalyticsServiceClient {

  /**
   * Records a user activity.
   *
   * @param userId       the user ID
   * @param activityType the activity type
   * @param description  the activity description
   * @param metadata     additional activity metadata
   */
  void recordActivity(Long userId, ActivityType activityType, String description, Map<String, Object> metadata);

  /**
   * Gets user activities within a time range.
   *
   * @param userId    the user ID
   * @param startTime the start time
   * @param endTime   the end time
   * @return list of activities
   */
  List<ActivityInfo> getUserActivities(Long userId, Instant startTime, Instant endTime);

  /**
   * Gets activity statistics for a user.
   *
   * @param userId    the user ID
   * @param startTime the start time
   * @param endTime   the end time
   * @return activity statistics
   */
  ActivityStats getUserActivityStats(Long userId, Instant startTime, Instant endTime);

  /**
   * Gets security-sensitive activities for a user.
   *
   * @param userId the user ID
   * @param limit  the maximum number of activities to return
   * @return list of security activities
   */
  List<ActivityInfo> getSecurityActivities(Long userId, int limit);

  /**
   * Gets suspicious activities across all users.
   *
   * @param limit the maximum number of activities to return
   * @return list of suspicious activities
   */
  List<ActivityInfo> getSuspiciousActivities(int limit);

  /**
   * Gets activity trends for dashboard display.
   *
   * @param timeRange the time range for trends
   * @return activity trends
   */
  ActivityTrends getActivityTrends(String timeRange);

  /**
   * Activity information record.
   */
  record ActivityInfo(
      Long id,
      Long userId,
      ActivityType activityType,
      String description,
      Instant occurredAt,
      String ipAddress,
      String userAgent,
      int activityScore,
      boolean isSecuritySensitive,
      boolean isSuspicious
  ) {
  }

  /**
   * Activity statistics record.
   */
  record ActivityStats(
      long totalActivities,
      long securitySensitiveActivities,
      long suspiciousActivities,
      double averageActivityScore,
      Map<ActivityType, Long> activityCounts
  ) {
  }

  /**
   * Activity trends record.
   */
  record ActivityTrends(
      Map<String, Long> dailyActivities,
      Map<ActivityType, Long> typeDistribution,
      List<String> topUsers,
      double growthRate
  ) {
  }
}
