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

package com.iqkv.sample.webmvc.dashboard.notification.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.notification.domain.Notification;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationChannel;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationStatus;
import com.iqkv.sample.webmvc.dashboard.notification.domain.NotificationType;
import com.iqkv.sample.webmvc.dashboard.notification.infrastructure.NotificationRepository;
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
 * Integration tests for NotificationController within the Notification bounded context.
 * <p>
 * Tests the presentation layer of the notification system, including
 * API endpoints for notification management and user notification retrieval.
 */
@AutoConfigureMockMvc
@IntegrationTest
class NotificationControllerIT extends BoundedContextTestBase {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  private Notification testNotification;

  @BeforeEach
  void setUp() {
    testNotification = createTestNotification();
  }

  @AfterEach
  void tearDown() {
    super.tearDown();
    cleanupBoundedContextData();
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.USER)
  @Transactional
  void shouldGetUserNotifications() throws Exception {
    // Given
    String userEmail = "test@example.com";
    testNotification.setRecipient(userEmail);
    notificationRepository.saveAndFlush(testNotification);

    // When & Then
    mockMvc
        .perform(get("/api/notifications")
            .param("recipient", userEmail)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].recipient").value(userEmail))
        .andExpect(jsonPath("$[0].subject").value(testNotification.getSubject()))
        .andExpect(jsonPath("$[0].type").value(testNotification.getType().toString()))
        .andExpect(jsonPath("$[0].status").value(testNotification.getStatus().toString()));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetAllNotifications() throws Exception {
    // Given
    notificationRepository.saveAndFlush(testNotification);

    // When & Then
    mockMvc
        .perform(get("/api/admin/notifications")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].recipient").value(testNotification.getRecipient()))
        .andExpect(jsonPath("$[0].subject").value(testNotification.getSubject()));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.USER)
  @Transactional
  void shouldReturnEmptyListForUserWithNoNotifications() throws Exception {
    // Given
    String userEmail = "nonotifications@example.com";

    // When & Then
    mockMvc
        .perform(get("/api/notifications")
            .param("recipient", userEmail)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldRetryFailedNotifications() throws Exception {
    // Given
    testNotification.markAsFailed("Network error");
    notificationRepository.saveAndFlush(testNotification);

    // When & Then
    mockMvc
        .perform(post("/api/admin/notifications/retry-failed")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // Verify notification status was reset
    Notification updatedNotification = notificationRepository.findById(testNotification.getId()).orElseThrow();
    assertThat(updatedNotification.getStatus()).isEqualTo(NotificationStatus.PENDING);
    assertThat(updatedNotification.getErrorMessage()).isNull();
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldSendPendingNotifications() throws Exception {
    // Given
    notificationRepository.saveAndFlush(testNotification);

    // When & Then
    mockMvc
        .perform(post("/api/admin/notifications/send-pending")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // Verify notification was processed
    Notification processedNotification = notificationRepository.findById(testNotification.getId()).orElseThrow();
    assertThat(processedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(processedNotification.getSentAt()).isNotNull();
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetNotificationsByStatus() throws Exception {
    // Given
    testNotification.setStatus(NotificationStatus.FAILED);
    notificationRepository.saveAndFlush(testNotification);

    // When & Then
    mockMvc
        .perform(get("/api/admin/notifications")
            .param("status", "FAILED")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("FAILED"));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
  @Transactional
  void shouldGetNotificationsByType() throws Exception {
    // Given
    testNotification.setType(NotificationType.ACTIVATION);
    notificationRepository.saveAndFlush(testNotification);

    // When & Then
    mockMvc
        .perform(get("/api/admin/notifications")
            .param("type", "ACTIVATION")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].type").value("ACTIVATION"));
  }

  @Test
  @WithMockUser(authorities = AuthoritiesConstants.USER)
  @Transactional
  void shouldDenyAccessToAdminEndpoints() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/admin/notifications"))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(post("/api/admin/notifications/retry-failed"))
        .andExpect(status().isForbidden());
  }

  @Test
  @Transactional
  void shouldRequireAuthenticationForUserEndpoints() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/notifications"))
        .andExpect(status().isUnauthorized());
  }

  @Override
  protected void cleanupBoundedContextData() {
    notificationRepository.deleteAll();
  }

  private Notification createTestNotification() {
    Notification notification = new Notification();
    notification.setRecipient("test@example.com");
    notification.setSubject("Test Notification");
    notification.setContent("This is a test notification content");
    notification.setType(NotificationType.WELCOME);
    notification.setChannel(NotificationChannel.EMAIL);
    notification.setStatus(NotificationStatus.PENDING);
    notification.setRetryCount(0);
    return notification;
  }
}
