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
import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivityDomainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Repository adapter that implements the domain repository interface
 * and delegates to the JPA repository.
 */
@Component
public class UserActivityRepositoryAdapter implements UserActivityDomainRepository {

  private final UserActivityRepository userActivityRepository;

  public UserActivityRepositoryAdapter(UserActivityRepository userActivityRepository) {
    this.userActivityRepository = userActivityRepository;
  }

  @Override
  public UserActivity save(UserActivity userActivity) {
    return userActivityRepository.save(userActivity);
  }

  @Override
  public Optional<UserActivity> findById(Long id) {
    return userActivityRepository.findById(id);
  }

  @Override
  public Page<UserActivity> findByUserId(Long userId, Pageable pageable) {
    return userActivityRepository.findByUserId(userId, pageable);
  }

  @Override
  public Page<UserActivity> findByUserIdAndActivityType(Long userId, ActivityType activityType, Pageable pageable) {
    return userActivityRepository.findByUserIdAndActivityType(userId, activityType, pageable);
  }

  @Override
  public Page<UserActivity> findByUserIdAndOccurredAtBetween(Long userId, Instant startTime, Instant endTime, Pageable pageable) {
    return userActivityRepository.findByUserIdAndOccurredAtBetween(userId, startTime, endTime, pageable);
  }

  @Override
  public Page<UserActivity> findSecuritySensitiveActivities(Long userId, Pageable pageable) {
    return userActivityRepository.findByUserIdAndActivityTypeIn(
        userId,
        List.of(ActivityType.LOGIN, ActivityType.LOGOUT, ActivityType.FAILED_LOGIN, ActivityType.PASSWORD_CHANGE),
        pageable
    );
  }

  @Override
  public Page<UserActivity> findSuspiciousActivities(Pageable pageable) {
    return userActivityRepository.findByActivityType(ActivityType.FAILED_LOGIN, pageable);
  }

  @Override
  public List<UserActivity> findBySessionId(String sessionId) {
    return userActivityRepository.findBySessionId(sessionId);
  }

  @Override
  public long countByUserIdAndActivityTypeAndOccurredAtBetween(Long userId, ActivityType activityType, Instant startTime, Instant endTime) {
    return userActivityRepository.countByUserIdAndActivityTypeAndOccurredAtBetween(userId, activityType, startTime, endTime);
  }

  @Override
  public long deleteActivitiesOlderThan(Instant cutoffDate) {
    return userActivityRepository.deleteByOccurredAtBefore(cutoffDate);
  }

  @Override
  public Optional<UserActivity> findMostRecentActivityByUserId(Long userId) {
    return userActivityRepository.findFirstByUserIdOrderByOccurredAtDesc(userId);
  }

  @Override
  public Page<UserActivity> findByIpAddress(String ipAddress, Pageable pageable) {
    return userActivityRepository.findByIpAddress(ipAddress, pageable);
  }
}
