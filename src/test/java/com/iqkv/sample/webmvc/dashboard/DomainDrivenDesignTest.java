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
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architecture tests for Domain-Driven Design principles.
 */
@AnalyzeClasses(packagesOf = DashboardApplication.class, importOptions = DoNotIncludeTests.class)
class DomainDrivenDesignTest {

  // Aggregate Rules
  @ArchTest
  static final ArchRule aggregatesShouldBeInDomainLayer = classes()
      .that().areAnnotatedWith("jakarta.persistence.Entity")
      .should().resideInAPackage("..domain..")
      .because("Aggregates should be in domain layer");

  @ArchTest
  static final ArchRule aggregatesShouldNotDependOnInfrastructure = noClasses()
      .that().areAnnotatedWith("jakarta.persistence.Entity")
      .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
      .because("Aggregates should not depend on infrastructure");

  // Value Object Rules
  @ArchTest
  static final ArchRule valueObjectsShouldBeImmutable = fields()
      .that().areDeclaredInClassesThat().areAnnotatedWith("jakarta.persistence.Embeddable")
      .should().beFinal()
      .because("Value objects should be immutable");

  @ArchTest
  static final ArchRule valueObjectsShouldBeInDomainLayer = classes()
      .that().areAnnotatedWith("jakarta.persistence.Embeddable")
      .should().resideInAPackage("..domain..")
      .because("Value objects should be in domain layer");

  // Domain Service Rules
  @ArchTest
  static final ArchRule domainServicesShouldBeInDomainLayer = classes()
      .that().haveSimpleNameEndingWith("DomainService")
      .should().resideInAPackage("..domain..")
      .because("Domain services should be in domain layer");

  @ArchTest
  static final ArchRule domainServicesShouldBeAnnotatedWithService = classes()
      .that().haveSimpleNameEndingWith("DomainService")
      .should().beAnnotatedWith("org.springframework.stereotype.Service")
      .because("Domain services should be Spring services");

  // Repository Rules
  @ArchTest
  static final ArchRule domainRepositoryInterfacesShouldBeInDomainLayer = classes()
      .that().haveSimpleNameEndingWith("DomainRepository")
      .and().areInterfaces()
      .should().resideInAPackage("..domain..")
      .because("Domain repository interfaces should be in domain layer");

  @ArchTest
  static final ArchRule repositoryImplementationsShouldBeInInfrastructureLayer = classes()
      .that().haveSimpleNameEndingWith("Repository")
      .and().areNotInterfaces()
      .and().doNotHaveSimpleName("AuthorityRepository") // Legacy exception
      .should().resideInAPackage("..infrastructure..")
      .because("Repository implementations should be in infrastructure layer");

  @ArchTest
  static final ArchRule repositoryAdaptersShouldImplementDomainRepositories = classes()
      .that().haveSimpleNameEndingWith("RepositoryAdapter")
      .should().implement(classes().that().haveSimpleNameEndingWith("DomainRepository"))
      .because("Repository adapters should implement domain repository interfaces");

  // Application Service Rules
  @ArchTest
  static final ArchRule applicationServicesShouldBeInApplicationLayer = classes()
      .that().haveSimpleNameEndingWith("Service")
      .and().areNotInterfaces()
      .and().doNotHaveSimpleNameEndingWith("DomainService")
      .and().doNotHaveSimpleName("MailService") // Legacy exception
      .and().doNotHaveSimpleName("SecurityMetersService") // Legacy exception
      .should().resideInAPackage("..application..")
      .because("Application services should be in application layer");

  @ArchTest
  static final ArchRule applicationServicesShouldBeAnnotatedWithService = classes()
      .that().resideInAPackage("..application..")
      .and().haveSimpleNameEndingWith("Service")
      .should().beAnnotatedWith("org.springframework.stereotype.Service")
      .because("Application services should be Spring services");

  @ArchTest
  static final ArchRule applicationServicesShouldBeTransactional = classes()
      .that().resideInAPackage("..application..")
      .and().haveSimpleNameEndingWith("Service")
      .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
      .because("Application services should be transactional");

  // Event Rules
  @ArchTest
  static final ArchRule domainEventsShouldBeRecords = classes()
      .that().haveSimpleNameEndingWith("Event")
      .and().resideInAPackage("..domain..")
      .should().beRecords()
      .because("Domain events should be immutable records");

  @ArchTest
  static final ArchRule eventHandlersShouldBeInApplicationLayer = classes()
      .that().haveSimpleNameEndingWith("EventHandler")
      .should().resideInAPackage("..application..")
      .because("Event handlers should be in application layer");

  @ArchTest
  static final ArchRule eventHandlerMethodsShouldBeAsync = methods()
      .that().areAnnotatedWith("org.springframework.context.event.EventListener")
      .should().beAnnotatedWith("org.springframework.scheduling.annotation.Async")
      .because("Event handler methods should be asynchronous");

  // DTO Rules
  @ArchTest
  static final ArchRule dtosShouldBeInApplicationLayer = classes()
      .that().haveSimpleNameEndingWith("DTO")
      .should().resideInAPackage("..application..")
      .or().resideInAPackage("..service..") // Legacy compatibility
      .because("DTOs should be in application layer");

  @ArchTest
  static final ArchRule dtosShouldNotBeInDomainLayer = noClasses()
      .that().haveSimpleNameEndingWith("DTO")
      .should().resideInAPackage("..domain..")
      .because("DTOs should not be in domain layer");

  // Controller Rules
  @ArchTest
  static final ArchRule controllersShouldBeInPresentationLayer = classes()
      .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
      .should().resideInAPackage("..presentation..")
      .or().resideInAPackage("..web..") // Legacy compatibility
      .because("Controllers should be in presentation layer");

  @ArchTest
  static final ArchRule controllersShouldNotAccessDomainDirectly = noClasses()
      .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
      .should().dependOnClassesThat().resideInAPackage("..domain..")
      .because("Controllers should not access domain layer directly");

  // Configuration Rules
  @ArchTest
  static final ArchRule configurationClassesShouldBeInConfigOrInfrastructurePackage = classes()
      .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
      .should().resideInAPackage("..config..")
      .or().resideInAPackage("..infrastructure..")
      .because("Configuration classes should be in config or infrastructure packages");

  // Dependency Injection Rules
  @ArchTest
  static final ArchRule servicesShouldUseConstructorInjection = classes()
      .that().haveSimpleNameEndingWith("Service")
      .should().haveOnlyFinalFields()
      .because("Services should use constructor injection with final fields");

  @ArchTest
  static final ArchRule noFieldInjection = noClasses()
      .should().haveFieldsAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
      .because("Field injection should not be used, prefer constructor injection");

  // Bounded Context Rules
  @ArchTest
  static final ArchRule boundedContextPackagesShouldBeAnnotated = classes()
      .that().haveSimpleName("package-info")
      .and().resideInAPackage("..usermanagement")
      .or().resideInAPackage("..notification")
      .should().beAnnotatedWith("org.springframework.modulith.ApplicationModule")
      .because("Bounded context packages should be annotated with @ApplicationModule");

  // Security Rules
  @ArchTest
  static final ArchRule adminEndpointsShouldBeSecured = methods()
      .that().areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
      .or().areAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
      .or().areAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
      .and().areDeclaredInClassesThat().resideInAPackage("..presentation..")
      .should().beAnnotatedWith("org.springframework.security.access.prepost.PreAuthorize")
      .because("Admin endpoints should be secured with @PreAuthorize");

  // Validation Rules
  @ArchTest
  static final ArchRule dtosShouldHaveValidation = classes()
      .that().haveSimpleNameEndingWith("DTO")
      .and().resideInAPackage("..application..")
      .should().haveFieldsAnnotatedWith("jakarta.validation.constraints.NotNull")
      .or().haveFieldsAnnotatedWith("jakarta.validation.constraints.NotBlank")
      .or().haveFieldsAnnotatedWith("jakarta.validation.constraints.Email")
      .or().haveFieldsAnnotatedWith("jakarta.validation.constraints.Size")
      .because("DTOs should have validation annotations");

  // Naming Convention Rules
  @ArchTest
  static final ArchRule aggregatesShouldFollowNamingConvention = classes()
      .that().areAnnotatedWith("jakarta.persistence.Entity")
      .and().resideInAPackage("..domain..")
      .should().haveSimpleNameNotEndingWith("Entity")
      .because("Domain entities should not have 'Entity' suffix");

  @ArchTest
  static final ArchRule valueObjectsShouldFollowNamingConvention = classes()
      .that().areAnnotatedWith("jakarta.persistence.Embeddable")
      .should().haveSimpleNameNotEndingWith("ValueObject")
      .and().haveSimpleNameNotEndingWith("VO")
      .because("Value objects should not have 'ValueObject' or 'VO' suffix");
}