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

package com.iqkv.sample.webmvc.dashboard.shared.testutil;

import java.util.Objects;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for bounded context integration tests.
 * <p>
 * Provides common setup and teardown functionality for DDD bounded context tests,
 * including cache management and test isolation.
 */
@IntegrationTest
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "logging.level.org.springframework.web=WARN",
    "logging.level.com.iqkv.sample.webmvc.dashboard=INFO"
})
public abstract class BoundedContextTestBase {

  @Autowired(required = false)
  private CacheManager cacheManager;

  /**
   * Clears all caches after each test to ensure test isolation.
   * Subclasses should call super.tearDown() if they override this method.
   */
  @AfterEach
  protected void tearDown() {
    clearAllCaches();
  }

  /**
   * Clears all application caches to ensure clean state between tests.
   */
  protected void clearAllCaches() {
    if (cacheManager != null) {
      cacheManager.getCacheNames()
          .stream()
          .map(cacheManager::getCache)
          .filter(Objects::nonNull)
          .forEach(cache -> cache.clear());
    }
  }

  /**
   * Template method for bounded context specific cleanup.
   * Subclasses should implement this to clean up their domain-specific data.
   */
  protected abstract void cleanupBoundedContextData();
}
