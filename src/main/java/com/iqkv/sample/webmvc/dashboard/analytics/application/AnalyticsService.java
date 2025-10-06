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

import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivityDomainRepository;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for analytics operations.
 * Handles user activity tracking and cross-bounded context analytics.
 */
@Service
@Transactional
public class AnalyticsService {

  private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);

  private final UserActivityDomainRepository userActivityRepository;

  public AnalyticsService(UserActivityDomainRepository userActivityRepository) {
    this.userActivityRepository = userActivityRepository;
  }

  /**
   * Records user activity.
   *
   * @param activity the user activity to record
   */
  public void recordActivity(UserActivity activity) {
    userActivityRepository.save(activity);
    LOG.debug("Recorded user activity: {}", activity.getActivityType());
  }

  /**
   * Handles user registration events for analytics.
   *
   * @param event the user registered event
   */
  @EventListener
  @Async
  public void handleUserRegistered(UserRegisteredEvent event) {
    LOG.info("Recording registration analytics for user: {}", event.login());

    UserActivity activity = new UserActivity();
    activity.setUserId(event.userId());
    activity.setActivityType(ActivityType.REGISTRATION);
    activity.setDescription("User registered: " + event.login());
    activity.setOccurredAt(event.occurredOn());

    userActivityRepository.save(activity);
    LOG.debug("Registration analytics recorded for user: {}", event.userId());
  }

  /**
   * Handles user activation events for analytics.
   *
   * @param event the user activated event
   */
  @EventListener
  @Async
  public void handleUserActivated(UserActivatedEvent event) {
    LOG.info("Recording activation analytics for user: {}", event.login());

    UserActivity activity = new UserActivity();
    activity.setUserId(event.userId());
    activity.setActivityType(ActivityType.ACCOUNT_ACTIVATION);
    activity.setDescription("User activated account: " + event.login());
    activity.setOccurredAt(event.occurredOn());

    userActivityRepository.save(activity);
    LOG.debug("Activation analytics recorded for user: {}", event.userId());
  }

  /**
   * Handles password reset events for analytics.
   *
   * @param event the password reset requested event
   */
  @EventListener
  @Async
  public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
    LOG.info("Recording password reset analytics for user: {}", event.userId());

    UserActivity activity = new UserActivity();
    activity.setUserId(event.userId());
    activity.setActivityType(ActivityType.PASSWORD_RESET_REQUEST);
    activity.setDescription("Password reset requested");
    activity.setOccurredAt(event.occurredOn());

    userActivityRepository.save(activity);
    LOG.debug("Password reset analytics recorded for user: {}", event.userId());
  }

  /**
   * Records user login activity.
   *
   * @param userId    the user ID
   * @param sessionId the session ID
   * @param ipAddress the IP address
   * @param userAgent the user agent
   */
  public void recordLogin(Long userId, String sessionId, String ipAddress, String userAgent) {
    UserActivity activity = UserActivity.recordLogin(userId, sessionId, ipAddress, userAgent);
    userActivityRepository.save(activity);
    LOG.debug("Login activity recorded for user: {}", userId);
  }

  /**
   * Records user logout activity.
   *
   * @param userId    the user ID
   * @param sessionId the session ID
   */
  public void recordLogout(Long userId, String sessionId) {
    UserActivity activity = UserActivity.recordLogout(userId, sessionId);
    userActivityRepository.save(activity);
    LOG.debug("Logout activity recorded for user: {}", userId);
  }
}
