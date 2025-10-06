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

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Modulith architecture tests to validate bounded context modularity and dependencies.
 * <p>
 * These tests ensure that our DDD bounded contexts are properly isolated and follow
 * modular architecture principles using Spring Modulith.
 */
class ModulithArchitectureTest {

  private final ApplicationModules modules = ApplicationModules.of(DashboardApplication.class);

  @Test
  void shouldHaveValidModularStructure() {
    // Verify that the application has a valid modular structure
    modules.verify();
  }

  @Test
  void shouldGenerateModuleDocumentation() {
    // Generate documentation for the modular structure
    new Documenter(modules)
        .writeDocumentation()
        .writeIndividualModulesAsPlantUml();
  }

  @Test
  void shouldPrintModuleStructure() {
    // Print the detected module structure for verification
    modules.forEach(System.out::println);
  }

  @Test
  void shouldValidateBoundedContexts() {
    // Verify that each bounded context is properly detected as a module
    modules.stream()
        .filter(module -> module.getName().equals("usermanagement"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("User Management bounded context not detected as module"));

    modules.stream()
        .filter(module -> module.getName().equals("security"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Security bounded context not detected as module"));

    modules.stream()
        .filter(module -> module.getName().equals("notification"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Notification bounded context not detected as module"));

    modules.stream()
        .filter(module -> module.getName().equals("analytics"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Analytics bounded context not detected as module"));
  }

  @Test
  void shouldValidateSharedKernel() {
    // Verify that shared kernel is properly detected
    modules.stream()
        .filter(module -> module.getName().equals("shared"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Shared kernel not detected as module"));
  }

  @Test
  void shouldValidateConfigurationModule() {
    // Verify that configuration module is properly detected
    modules.stream()
        .filter(module -> module.getName().equals("config"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Configuration module not detected"));
  }
}
