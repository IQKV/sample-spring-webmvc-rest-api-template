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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.ActivityType;
import com.iqkv.sample.webmvc.dashboard.analytics.domain.UserActivity;
import com.iqkv.sample.webmvc.dashboard.analytics.infrastructure.UserActivityRepository;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.BoundedContextTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AnalyticsController within the Analytics bounded context.
 * <p>
 * Tests the presentation layer of the analytics system, including
 * API endpoints for analytics data retrieval and activity tracking.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AnalyticsControllerIT extends BoundedContextTestBase {

  @Autowired
  private UserActivityRepository userActivityRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  private UserActivity testActivity;

  @BeforeEach
  void setUp() {
    testActivity = createTestActivity();
  }

  @AfterEach
  void tearDown() {
    super.tearDown();
    cleanupBoundedContextData();
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetUserActivities() throws Exception {
    // Given
    Long userId = 1L;
    testActivity.setUserId(userId);
    userActivityRepository.saveAndFlush(testActivity);

    // When & Then
    mockMvc
        .perform(get("/api/analytics/users/{userId}/activities", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId").value(userId.intValue()))
        .andExpect(jsonPath("$[0].activityType").value(testActivity.getActivityType().toString()));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetDailyActivityCounts() throws Exception {
    // Given
    LocalDate today = LocalDate.now();
    testActivity.setOccurredAt(today.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
    userActivityRepository.saveAndFlush(testActivity);

    // When & Then
    mockMvc
        .perform(get("/api/analytics/daily-counts")
            .param("startDate", today.toString())
            .param("endDate", today.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$." + today.toString()).value(1));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetActivityCountsByType() throws Exception {
    // Given
    LocalDate today = LocalDate.now();
    testActivity.setOccurredAt(today.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
    testActivity.setActivityType(ActivityType.LOGIN);
    userActivityRepository.saveAndFlush(testActivity);

    // When & Then
    mockMvc
        .perform(get("/api/analytics/activity-counts-by-type")
            .param("startDate", today.toString())
            .param("endDate", today.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.LOGIN").value(1));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetMostActiveUsers() throws Exception {
    // Given
    LocalDate today = LocalDate.now();
    testActivity.setUserId(1L);
    testActivity.setOccurredAt(today.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
    userActivityRepository.saveAndFlush(testActivity);

    // Create another activity for the same user
    UserActivity secondActivity = createTestActivity();
    secondActivity.setUserId(1L);
    secondActivity.setOccurredAt(today.atTime(12, 0).atZone(ZoneOffset.UTC).toInstant());
    userActivityRepository.saveAndFlush(secondActivity);

    // When & Then
    mockMvc
        .perform(get("/api/analytics/most-active-users")
            .param("startDate", today.toString())
            .param("endDate", today.toString())
            .param("limit", "5")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[0].activityCount").value(2));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldRecordUserActivity() throws Exception {
    // Given
    Map<String, Object> activityRequest = Map.of(
        "userId", 1L,
        "activityType", "LOGIN",
        "metadata", Map.of("ipAddress", "192.168.1.1")
    );

    // When & Then
    mockMvc
        .perform(post("/api/analytics/activities")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(activityRequest)))
        .andExpect(status().isCreated());

    // Verify activity was recorded
    var activities = userActivityRepository.findByUserId(1L);
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).getActivityType()).isEqualTo(ActivityType.LOGIN);
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.USER)
  @Transactional
  void shouldGetOwnUserActivities() throws Exception {
    // Given
    Long userId = 1L;
    testActivity.setUserId(userId);
    userActivityRepository.saveAndFlush(testActivity);

    // When & Then
    mockMvc
        .perform(get("/api/analytics/my-activities")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].activityType").value(testActivity.getActivityType().toString()));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.USER)
  @Transactional
  void shouldDenyAccessToAdminAnalytics() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/analytics/daily-counts"))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/api/analytics/most-active-users"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldValidateDateRangeParameters() throws Exception {
    // Given
    LocalDate today = LocalDate.now();
    LocalDate futureDate = today.plusDays(10);

    // When & Then - Should reject invalid date range
    mockMvc
        .perform(get("/api/analytics/daily-counts")
            .param("startDate", futureDate.toString())
            .param("endDate", today.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldHandleEmptyAnalyticsData() throws Exception {
    // Given
    LocalDate today = LocalDate.now();

    // When & Then - Should return empty results gracefully
    mockMvc
        .perform(get("/api/analytics/daily-counts")
            .param("startDate", today.toString())
            .param("endDate", today.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @Transactional
  void shouldRequireAuthenticationForAnalyticsEndpoints() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/analytics/my-activities"))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(get("/api/analytics/daily-counts"))
        .andExpect(status().isUnauthorized());
  }

  @Override
  protected void cleanupBoundedContextData() {
    userActivityRepository.deleteAll();
  }

  private UserActivity createTestActivity() {
    UserActivity activity = new UserActivity();
    activity.setUserId(1L);
    activity.setActivityType(ActivityType.LOGIN);
    activity.setOccurredAt(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant());
    activity.setMetadata(Map.of("ipAddress", "192.168.1.1", "userAgent", "Mozilla/5.0"));
    return activity;
  }
}
