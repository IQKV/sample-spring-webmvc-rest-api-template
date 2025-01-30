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

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class PostgreSqlTestContainer implements SqlTestContainer {

  private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlTestContainer.class);

  private PostgreSQLContainer<?> postgreSQLContainer;

  @Override
  public void destroy() {
    if (null != postgreSQLContainer && postgreSQLContainer.isRunning()) {
      postgreSQLContainer.stop();
    }
  }

  @Override
  public void afterPropertiesSet() {
    if (null == postgreSQLContainer) {
      postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.6")
          .withDatabaseName("Dashboard")
          .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
          .withLogConsumer(new Slf4jLogConsumer(LOG))
          .withReuse(true);
    }
    if (!postgreSQLContainer.isRunning()) {
      postgreSQLContainer.start();
    }
  }

  @Override
  public JdbcDatabaseContainer<?> getTestContainer() {
    return postgreSQLContainer;
  }
}
