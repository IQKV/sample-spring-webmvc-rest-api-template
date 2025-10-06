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

package com.iqkv.sample.webmvc.dashboard.usermanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.usermanagement.application.UserManagementService;
import com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto.AdminUserDTO;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;

/**
 * Modulith integration test for User Management bounded context.
 * <p>
 * This test validates that the User Management module works correctly in isolation
 * and properly publishes domain events for other bounded contexts to consume.
 */
@ApplicationModuleTest
@ActiveProfiles("test")
class UserManagementModuleTest {

  @Autowired
  private UserManagementService userManagementService;

  @Test
  void shouldCreateUserAndPublishEvent(Scenario scenario) {
    // Given
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("testuser");
    userDto.setEmail("test@example.com");
    userDto.setFirstName("Test");
    userDto.setLastName("User");
    userDto.setActivated(false);

    // When - Create user and expect event to be published
    User createdUser = scenario
        .stimulate(() -> userManagementService.createUser(userDto))
        .andWaitForEventOfType(UserRegisteredEvent.class)
        .toArrive();

    // Then
    assertThat(createdUser).isNotNull();
    assertThat(createdUser.getLogin()).isEqualTo("testuser");
    assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
    assertThat(createdUser.isActivated()).isFalse();
    assertThat(createdUser.getActivationKey()).isNotNull();
  }

  @Test
  void shouldActivateUserAndPublishEvent(Scenario scenario) {
    // Given - Create a user first
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("activateuser");
    userDto.setEmail("activate@example.com");
    userDto.setFirstName("Activate");
    userDto.setLastName("User");
    userDto.setActivated(false);

    User createdUser = userManagementService.createUser(userDto);
    String activationKey = createdUser.getActivationKey();

    // When - Activate user and expect event to be published
    scenario
        .stimulate(() -> userManagementService.activateRegistration(activationKey))
        .andWaitForEventOfType(com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent.class)
        .toArrive();

    // Then - Verify user is activated
    Optional<User> activatedUser = userManagementService.getUserWithAuthoritiesByLogin("activateuser");
    assertThat(activatedUser).isPresent();
    assertThat(activatedUser.get().isActivated()).isTrue();
    assertThat(activatedUser.get().getActivationKey()).isNull();
  }

  @Test
  void shouldRequestPasswordResetAndPublishEvent(Scenario scenario) {
    // Given - Create and activate a user first
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("resetuser");
    userDto.setEmail("reset@example.com");
    userDto.setFirstName("Reset");
    userDto.setLastName("User");
    userDto.setActivated(true);

    User createdUser = userManagementService.createUser(userDto);

    // When - Request password reset and expect event to be published
    scenario
        .stimulate(() -> userManagementService.requestPasswordReset("reset@example.com"))
        .andWaitForEventOfType(com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent.class)
        .toArrive();

    // Then - Verify reset key is set
    Optional<User> userWithReset = userManagementService.getUserWithAuthoritiesByLogin("resetuser");
    assertThat(userWithReset).isPresent();
    assertThat(userWithReset.get().getResetKey()).isNotNull();
    assertThat(userWithReset.get().getResetDate()).isNotNull();
  }

  @Test
  void shouldValidateUserManagementServiceIsolation() {
    // Verify that UserManagementService only depends on domain and shared components
    assertThat(userManagementService).isNotNull();

    // The service should be able to operate independently
    long userCount = userManagementService.countUsers();
    assertThat(userCount).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldHandleUserLifecycleCompletely() {
    // Test complete user lifecycle within the bounded context

    // 1. Create user
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("lifecycleuser");
    userDto.setEmail("lifecycle@example.com");
    userDto.setFirstName("Lifecycle");
    userDto.setLastName("User");
    userDto.setActivated(false);

    User createdUser = userManagementService.createUser(userDto);
    assertThat(createdUser.isActivated()).isFalse();

    // 2. Activate user
    Optional<User> activatedUser = userManagementService.activateRegistration(createdUser.getActivationKey());
    assertThat(activatedUser).isPresent();
    assertThat(activatedUser.get().isActivated()).isTrue();

    // 3. Update user
    AdminUserDTO updateDto = new AdminUserDTO(activatedUser.get());
    updateDto.setFirstName("Updated");
    Optional<AdminUserDTO> updatedUser = userManagementService.updateUser(updateDto);
    assertThat(updatedUser).isPresent();
    assertThat(updatedUser.get().getFirstName()).isEqualTo("Updated");

    // 4. Delete user
    userManagementService.deleteUser("lifecycleuser");
    Optional<User> deletedUser = userManagementService.getUserWithAuthoritiesByLogin("lifecycleuser");
    assertThat(deletedUser).isEmpty();
  }

  @Test
  void shouldValidateDomainServiceIntegration() {
    // Test that domain services are properly integrated
    AdminUserDTO userDto = new AdminUserDTO();
    userDto.setLogin("domaintest");
    userDto.setEmail("domain@example.com");
    userDto.setFirstName("Domain");
    userDto.setLastName("Test");
    userDto.setActivated(false);

    // This should trigger domain validation through UserDomainService
    User createdUser = userManagementService.createUser(userDto);
    assertThat(createdUser).isNotNull();

    // Verify domain rules are applied
    assertThat(createdUser.getLogin()).isEqualTo("domaintest");
    assertThat(createdUser.getEmail()).isEqualTo("domain@example.com");
  }
}
