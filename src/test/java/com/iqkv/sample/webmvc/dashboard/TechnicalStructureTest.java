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

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = DashboardApplication.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

  // DDD Onion Architecture Rules
  @ArchTest
  static final ArchRule respectsDomainDrivenDesignLayers = onionArchitecture()
      .domainModels("..domain..")
      .domainServices("..domain..")
      .applicationServices("..application..")
      .adapter("infrastructure", "..infrastructure..")
      .adapter("presentation", "..presentation..")
      .adapter("web", "..web..")
      .adapter("config", "..config..")
      .adapter("security", "..security..")
      .adapter("management", "..management..")

      .ignoreDependency(belongToAnyOf(DashboardApplication.class), alwaysTrue())
      .ignoreDependency(alwaysTrue(), belongToAnyOf(
          com.iqkv.sample.webmvc.dashboard.config.AppConstants.class,
          com.iqkv.sample.webmvc.dashboard.config.ApplicationProperties.class
      ));

  // Bounded Context Isolation Rules
  @ArchTest
  static final ArchRule boundedContextsShouldBeIsolated = noClasses()
      .that().resideInAPackage("..usermanagement..")
      .should().dependOnClassesThat().resideInAPackage("..notification..")
      .orShould().dependOnClassesThat().resideInAPackage("..otherboundedcontext..")
      .because("Bounded contexts should not directly depend on each other");

  @ArchTest
  static final ArchRule notificationContextShouldNotDependOnUserManagement = noClasses()
      .that().resideInAPackage("..notification..")
      .should().dependOnClassesThat().resideInAPackage("..usermanagement..")
      .because("Notification context should only listen to events, not directly depend on user management");

  // Domain Layer Rules
  @ArchTest
  static final ArchRule domainLayerShouldNotDependOnApplicationLayer = noClasses()
      .that().resideInAPackage("..domain..")
      .should().dependOnClassesThat().resideInAPackage("..application..")
      .because("Domain layer should not depend on application layer");

  @ArchTest
  static final ArchRule domainLayerShouldNotDependOnInfrastructureLayer = noClasses()
      .that().resideInAPackage("..domain..")
      .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
      .because("Domain layer should not depend on infrastructure layer");

  @ArchTest
  static final ArchRule domainLayerShouldNotDependOnPresentationLayer = noClasses()
      .that().resideInAPackage("..domain..")
      .should().dependOnClassesThat().resideInAPackage("..presentation..")
      .because("Domain layer should not depend on presentation layer");

  // Application Layer Rules
  @ArchTest
  static final ArchRule applicationLayerShouldNotDependOnInfrastructureLayer = noClasses()
      .that().resideInAPackage("..application..")
      .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
      .except(classes().that().resideInAPackage("..application..").and().haveSimpleNameEndingWith("EventHandler"))
      .because("Application layer should not depend on infrastructure layer except for event handlers");

  @ArchTest
  static final ArchRule applicationLayerShouldNotDependOnPresentationLayer = noClasses()
      .that().resideInAPackage("..application..")
      .should().dependOnClassesThat().resideInAPackage("..presentation..")
      .because("Application layer should not depend on presentation layer");

  // Infrastructure Layer Rules
  @ArchTest
  static final ArchRule infrastructureLayerShouldNotDependOnPresentationLayer = noClasses()
      .that().resideInAPackage("..infrastructure..")
      .should().dependOnClassesThat().resideInAPackage("..presentation..")
      .because("Infrastructure layer should not depend on presentation layer");

  // Repository Rules
  @ArchTest
  static final ArchRule repositoriesShouldBeInInfrastructureLayer = classes()
      .that().haveSimpleNameEndingWith("Repository")
      .and().areNotInterfaces()
      .should().resideInAPackage("..infrastructure..")
      .because("Repository implementations should be in infrastructure layer");

  @ArchTest
  static final ArchRule domainRepositoryInterfacesShouldBeInDomainLayer = classes()
      .that().haveSimpleNameEndingWith("Repository")
      .and().areInterfaces()
      .and().haveSimpleNameContaining("Domain")
      .should().resideInAPackage("..domain..")
      .because("Domain repository interfaces should be in domain layer");

  // Service Rules
  @ArchTest
  static final ArchRule applicationServicesShouldBeInApplicationLayer = classes()
      .that().haveSimpleNameEndingWith("Service")
      .and().areNotInterfaces()
      .and().doNotHaveSimpleName("MailService")
      .and().doNotHaveSimpleName("SecurityMetersService")
      .should().resideInAPackage("..application..")
      .or().resideInAPackage("..domain..")
      .because("Services should be in application or domain layer");

  @ArchTest
  static final ArchRule domainServicesShouldBeInDomainLayer = classes()
      .that().haveSimpleNameEndingWith("DomainService")
      .should().resideInAPackage("..domain..")
      .because("Domain services should be in domain layer");

  // Controller Rules
  @ArchTest
  static final ArchRule controllersShouldBeInPresentationLayer = classes()
      .that().haveSimpleNameEndingWith("Controller")
      .should().resideInAPackage("..presentation..")
      .or().resideInAPackage("..web..")
      .because("Controllers should be in presentation or web layer");

  // Entity Rules
  @ArchTest
  static final ArchRule entitiesShouldBeInDomainLayer = classes()
      .that().areAnnotatedWith("jakarta.persistence.Entity")
      .should().resideInAPackage("..domain..")
      .because("JPA entities should be in domain layer");

  // Event Rules
  @ArchTest
  static final ArchRule domainEventsShouldBeInDomainLayer = classes()
      .that().haveSimpleNameEndingWith("Event")
      .should().resideInAPackage("..domain..")
      .because("Domain events should be in domain layer");

  // DTO Rules
  @ArchTest
  static final ArchRule dtosShouldBeInApplicationLayer = classes()
      .that().haveSimpleNameEndingWith("DTO")
      .should().resideInAPackage("..application..")
      .or().resideInAPackage("..service..")
      .because("DTOs should be in application layer");

  // Configuration Rules
  @ArchTest
  static final ArchRule configurationClassesShouldBeInConfigPackage = classes()
      .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
      .should().resideInAPackage("..config..")
      .or().resideInAPackage("..infrastructure..")
      .because("Configuration classes should be in config or infrastructure packages");

  // Legacy Layer Compatibility (for gradual migration)
  @ArchTest
  static final ArchRule legacyServiceLayerCanAccessDomainAndRepository = noClasses()
      .that().resideInAPackage("..service..")
      .should().dependOnClassesThat().resideInAPackage("..usermanagement..")
      .because("Legacy service layer should migrate to use bounded contexts");
}
