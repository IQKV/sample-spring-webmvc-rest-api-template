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

package com.iqkv.sample.webmvc.dashboard.analytics.presentation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.analytics.application.AnalyticsService;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.infrastructure.UserActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for analytics and user activity tracking.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

  private static final Logger LOG = LoggerFactory.getLogger(AnalyticsController.class);

  private final AnalyticsService analyticsService;
  private final UserActivityRepository userActivityRepository;

  public AnalyticsController(AnalyticsService analyticsService, UserActivityRepository userActivityRepository) {
    this.analyticsService = analyticsService;
    this.userActivityRepository = userActivityRepository;
  }

  /**
   * Gets user activities with pagination.
   *
   * @param userId   the user ID
   * @param pageable pagination information
   * @return page of user activities
   */
  @GetMapping("/users/{userId}/activities")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Page<UserActivity>> getUserActivities(
      @PathVariable Long userId,
      Pageable pageable) {
    LOG.debug("REST request to get activities for user: {}", userId);

    Page<UserActivity> activities = userActivityRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable);
    return ResponseEntity.ok(activities);
  }

  /**
   * Gets activity statistics for the dashboard.
   *
   * @param days number of days to look back
   * @return activity statistics
   */
  @GetMapping("/dashboard/stats")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestParam(defaultValue = "7") int days) {
    LOG.debug("REST request to get dashboard statistics for {} days", days);

    Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
    Instant endDate = Instant.now();

    long loginCount = userActivityRepository.countByActivityTypeAndDateRange(
        ActivityType.LOGIN, startDate, endDate);
    long registrationCount = userActivityRepository.countByActivityTypeAndDateRange(
        ActivityType.REGISTRATION, startDate, endDate);
    long profileUpdateCount = userActivityRepository.countByActivityTypeAndDateRange(
        ActivityType.PROFILE_UPDATE, startDate, endDate);

    Map<String, Object> stats = Map.of(
        "period_days", days,
        "login_count", loginCount,
        "registration_count", registrationCount,
        "profile_update_count", profileUpdateCount,
        "total_activities", loginCount + registrationCount + profileUpdateCount
    );

    return ResponseEntity.ok(stats);
  }

  /**
   * Gets recent activities for the dashboard.
   *
   * @param limit maximum number of activities to return
   * @return list of recent activities
   */
  @GetMapping("/dashboard/recent")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<List<UserActivity>> getRecentActivities(@RequestParam(defaultValue = "10") int limit) {
    LOG.debug("REST request to get {} recent activities", limit);

    List<UserActivity> activities = userActivityRepository.findRecentActivities(limit);
    return ResponseEntity.ok(activities);
  }

  /**
   * Gets security-sensitive activities for a user.
   *
   * @param userId   the user ID
   * @param pageable pagination information
   * @return page of security activities
   */
  @GetMapping("/users/{userId}/security-activities")
  @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
  public ResponseEntity<Page<UserActivity>> getSecurityActivities(
      @PathVariable Long userId,
      Pageable pageable) {
    LOG.debug("REST request to get security activities for user: {}", userId);

    List<ActivityType> securityTypes = List.of(
        ActivityType.LOGIN,
        ActivityType.LOGOUT,
        ActivityType.FAILED_LOGIN,
        ActivityType.PASSWORD_CHANGE,
        ActivityType.PASSWORD_RESET_REQUEST
    );

    Page<UserActivity> activities = userActivityRepository
        .findByUserIdAndActivityTypeInOrderByOccurredAtDesc(userId, securityTypes, pageable);
    return ResponseEntity.ok(activities);
  }
}
