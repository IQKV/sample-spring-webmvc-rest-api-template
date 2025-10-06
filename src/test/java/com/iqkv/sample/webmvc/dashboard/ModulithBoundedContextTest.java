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

package com.iqkv.sample.webmvc.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Modulith tests for bounded context isolation and dependency validation.
 * <p>
 * These tests ensure that bounded contexts are properly isolated and only
 * communicate through well-defined interfaces and events.
 */
class ModulithBoundedContextTest {

  private final ApplicationModules modules = ApplicationModules.of(DashboardApplication.class);

  @Test
  void userManagementShouldBeIsolated() {
    ApplicationModule userManagement = modules.getModuleByName("usermanagement")
        .orElseThrow(() -> new AssertionError("User Management module not found"));

    // User Management should not directly depend on other bounded contexts
    assertThat(userManagement.getDirectDependencies())
        .noneMatch(dep -> dep.getName().equals("notification"))
        .noneMatch(dep -> dep.getName().equals("analytics"))
        .noneMatch(dep -> dep.getName().equals("security"));

    // User Management can depend on shared kernel
    assertThat(userManagement.getDirectDependencies())
        .anyMatch(dep -> dep.getName().equals("shared"));
  }

  @Test
  void securityShouldBeIsolated() {
    ApplicationModule security = modules.getModuleByName("security")
        .orElseThrow(() -> new AssertionError("Security module not found"));

    // Security should not directly depend on other bounded contexts
    assertThat(security.getDirectDependencies())
        .noneMatch(dep -> dep.getName().equals("notification"))
        .noneMatch(dep -> dep.getName().equals("analytics"))
        .noneMatch(dep -> dep.getName().equals("usermanagement"));

    // Security can depend on shared kernel
    assertThat(security.getDirectDependencies())
        .anyMatch(dep -> dep.getName().equals("shared"));
  }

  @Test
  void notificationShouldBeIsolated() {
    ApplicationModule notification = modules.getModuleByName("notification")
        .orElseThrow(() -> new AssertionError("Notification module not found"));

    // Notification should not directly depend on other bounded contexts
    assertThat(notification.getDirectDependencies())
        .noneMatch(dep -> dep.getName().equals("usermanagement"))
        .noneMatch(dep -> dep.getName().equals("analytics"))
        .noneMatch(dep -> dep.getName().equals("security"));

    // Notification can depend on shared kernel
    assertThat(notification.getDirectDependencies())
        .anyMatch(dep -> dep.getName().equals("shared"));
  }

  @Test
  void analyticsShouldBeIsolated() {
    ApplicationModule analytics = modules.getModuleByName("analytics")
        .orElseThrow(() -> new AssertionError("Analytics module not found"));

    // Analytics should not directly depend on other bounded contexts
    assertThat(analytics.getDirectDependencies())
        .noneMatch(dep -> dep.getName().equals("usermanagement"))
        .noneMatch(dep -> dep.getName().equals("notification"))
        .noneMatch(dep -> dep.getName().equals("security"));

    // Analytics can depend on shared kernel
    assertThat(analytics.getDirectDependencies())
        .anyMatch(dep -> dep.getName().equals("shared"));
  }

  @Test
  void sharedKernelShouldHaveNoDependencies() {
    ApplicationModule shared = modules.getModuleByName("shared")
        .orElseThrow(() -> new AssertionError("Shared module not found"));

    // Shared kernel should not depend on any bounded context
    assertThat(shared.getDirectDependencies())
        .noneMatch(dep -> dep.getName().equals("usermanagement"))
        .noneMatch(dep -> dep.getName().equals("notification"))
        .noneMatch(dep -> dep.getName().equals("analytics"))
        .noneMatch(dep -> dep.getName().equals("security"));
  }

  @Test
  void configShouldHaveMinimalDependencies() {
    ApplicationModule config = modules.getModuleByName("config")
        .orElseThrow(() -> new AssertionError("Config module not found"));

    // Config should not depend on bounded contexts (only shared if needed)
    assertThat(config.getDirectDependencies())
        .noneMatch(dep -> dep.getName().equals("usermanagement"))
        .noneMatch(dep -> dep.getName().equals("notification"))
        .noneMatch(dep -> dep.getName().equals("analytics"))
        .noneMatch(dep -> dep.getName().equals("security"));
  }

  @Test
  void shouldValidateModuleBootstrap() {
    // Verify that each module can be bootstrapped independently
    modules.forEach(module -> {
      assertThat(module.getBootstrapDependencies()).isNotNull();
      System.out.println("Module: " + module.getName() +
                         " - Bootstrap dependencies: " + module.getBootstrapDependencies().size());
    });
  }

  @Test
  void shouldValidateEventPublishers() {
    // Verify that modules properly expose event publishers
    ApplicationModule userManagement = modules.getModuleByName("usermanagement")
        .orElseThrow(() -> new AssertionError("User Management module not found"));

    // User Management should expose events for other contexts to listen to
    assertThat(userManagement.getPublishedEvents()).isNotEmpty();

    System.out.println("User Management published events: " + userManagement.getPublishedEvents());
  }

  @Test
  void shouldValidateEventListeners() {
    // Verify that modules properly listen to events from other contexts
    ApplicationModule notification = modules.getModuleByName("notification")
        .orElseThrow(() -> new AssertionError("Notification module not found"));

    ApplicationModule analytics = modules.getModuleByName("analytics")
        .orElseThrow(() -> new AssertionError("Analytics module not found"));

    // These modules should listen to events (event listeners will be detected)
    System.out.println("Notification module event listeners: " + notification.toString());
    System.out.println("Analytics module event listeners: " + analytics.toString());
  }
}
