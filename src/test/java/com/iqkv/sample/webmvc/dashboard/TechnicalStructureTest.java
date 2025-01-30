/*
 * Copyright 2025 IQKV Team.
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
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = DashboardApplication.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

  // prettier-ignore
  @ArchTest
  static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
      .consideringAllDependencies()
      .layer("Config").definedBy("..config..")
      .layer("Web").definedBy("..web..")
      .optionalLayer("Service").definedBy("..service..")
      .layer("Security").definedBy("..security..")
      .optionalLayer("Persistence").definedBy("..repository..")
      .layer("Domain").definedBy("..domain..")

      .whereLayer("Config").mayNotBeAccessedByAnyLayer()
      .whereLayer("Web").mayOnlyBeAccessedByLayers("Config")
      .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config")
      .whereLayer("Security").mayOnlyBeAccessedByLayers("Config", "Service", "Web")
      .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Security", "Web", "Config")
      .whereLayer("Domain").mayOnlyBeAccessedByLayers("Persistence", "Service", "Security", "Web", "Config")

      .ignoreDependency(belongToAnyOf(DashboardApplication.class), alwaysTrue())
      .ignoreDependency(alwaysTrue(), belongToAnyOf(
          com.iqkv.sample.webmvc.dashboard.config.Constants.class,
          com.iqkv.sample.webmvc.dashboard.config.ApplicationProperties.class
      ));
}
