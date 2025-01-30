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

package com.iqkv.sample.webmvc.dashboard.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

public class SqlTestContainersSpringContextCustomizerFactory implements ContextCustomizerFactory {

  private static final Logger LOG = LoggerFactory.getLogger(SqlTestContainersSpringContextCustomizerFactory.class);

  private static SqlTestContainer prodTestContainer;

  @Override
  public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
    return new ContextCustomizer() {
      @Override
      public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        TestPropertyValues testValues = TestPropertyValues.empty();
        EmbeddedSQL sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL.class);
        if (null != sqlAnnotation) {
          LOG.debug("detected the EmbeddedSQL annotation on class {}", testClass.getName());
          LOG.info("Warming up the sql database");
          if (null == prodTestContainer) {
            try {
              Class<? extends SqlTestContainer> containerClass = (Class<? extends SqlTestContainer>) Class.forName(
                  this.getClass().getPackageName() + ".PostgreSqlTestContainer"
              );
              prodTestContainer = beanFactory.createBean(containerClass);
              beanFactory.registerSingleton(containerClass.getName(), prodTestContainer);
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          }
          testValues = testValues.and("spring.datasource.url=" + prodTestContainer.getTestContainer().getJdbcUrl() + "");
          testValues = testValues.and("spring.datasource.username=" + prodTestContainer.getTestContainer().getUsername());
          testValues = testValues.and("spring.datasource.password=" + prodTestContainer.getTestContainer().getPassword());
        }
        testValues.applyTo(context);
      }

      @Override
      public int hashCode() {
        return SqlTestContainer.class.getName().hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
      }
    };
  }
}
