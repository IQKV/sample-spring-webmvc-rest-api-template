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

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Architecture tests to enforce DDD bounded context boundaries and layered architecture.
 * <p>
 * These tests ensure that:
 * - Bounded contexts are properly isolated
 * - Domain layer doesn't depend on infrastructure
 * - Application layer orchestrates domain and infrastructure
 * - Presentation layer only depends on application layer
 */
class BoundedContextArchitectureTest {

  private JavaClasses importedClasses;

  @BeforeEach
  void setUp() {
    importedClasses = new ClassFileImporter()
        .importPackages("com.iqkv.sample.webmvc.dashboard");
  }

  @Test
  void domainLayerShouldNotDependOnInfrastructure() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..presentation..");

    rule.check(importedClasses);
  }

  @Test
  void domainLayerShouldNotDependOnApplication() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..application..");

    rule.check(importedClasses);
  }

  @Test
  void applicationLayerShouldNotDependOnPresentation() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAPackage("..presentation..");

    rule.check(importedClasses);
  }

  @Test
  void infrastructureLayerShouldNotDependOnPresentation() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..infrastructure..")
        .should().dependOnClassesThat()
        .resideInAPackage("..presentation..");

    rule.check(importedClasses);
  }

  @Test
  void boundedContextsShouldNotDependOnEachOtherDirectly() {
    // User Management should not depend on other bounded contexts directly
    ArchRule userManagementRule = noClasses()
        .that().resideInAPackage("..usermanagement..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..notification..", "..analytics..", "..security..");

    userManagementRule.check(importedClasses);

    // Notification should not depend on other bounded contexts directly
    ArchRule notificationRule = noClasses()
        .that().resideInAPackage("..notification..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..usermanagement..", "..analytics..", "..security..");

    notificationRule.check(importedClasses);

    // Analytics should not depend on other bounded contexts directly
    ArchRule analyticsRule = noClasses()
        .that().resideInAPackage("..analytics..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..usermanagement..", "..notification..", "..security..");

    analyticsRule.check(importedClasses);

    // Security should not depend on other bounded contexts directly
    ArchRule securityRule = noClasses()
        .that().resideInAPackage("..security..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..usermanagement..", "..notification..", "..analytics..");

    securityRule.check(importedClasses);
  }

  @Test
  void repositoriesShouldBeInInfrastructureLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*Repository")
        .and().areNotInterfaces()
        .should().resideInAPackage("..infrastructure..");

    rule.check(importedClasses);
  }

  @Test
  void controllersShouldBeInPresentationLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*Controller")
        .should().resideInAPackage("..presentation..");

    rule.check(importedClasses);
  }

  @Test
  void servicesShouldBeInApplicationLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*Service")
        .and().areNotInterfaces()
        .should().resideInAPackage("..application..");

    rule.check(importedClasses);
  }

  @Test
  void domainEntitiesShouldBeInDomainLayer() {
    ArchRule rule = classes()
        .that().areAnnotatedWith(jakarta.persistence.Entity.class)
        .should().resideInAPackage("..domain..");

    rule.check(importedClasses);
  }

  @Test
  void eventHandlersShouldBeInApplicationLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*EventHandler")
        .should().resideInAPackage("..application..");

    rule.check(importedClasses);
  }

  @Test
  void domainEventsShouldBeInDomainLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*Event")
        .and().resideInAPackage("..domain..")
        .should().resideInAPackage("..domain.event..");

    rule.check(importedClasses);
  }

  @Test
  void valueObjectsShouldBeInDomainLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*VO")
        .or().haveNameMatching(".*ValueObject")
        .should().resideInAPackage("..domain..");

    rule.check(importedClasses);
  }

  @Test
  void dtosShouldBeInApplicationLayer() {
    ArchRule rule = classes()
        .that().haveNameMatching(".*DTO")
        .should().resideInAPackage("..application.dto..");

    rule.check(importedClasses);
  }
}
