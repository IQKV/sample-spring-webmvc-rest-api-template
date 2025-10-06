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

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.Callable;

import org.springframework.context.ApplicationEventPublisher;

/**
 * Utility class for testing domain events in DDD bounded contexts.
 * <p>
 * Provides helper methods for publishing events and waiting for asynchronous
 * event processing to complete in integration tests.
 */
public final class DomainEventTestUtil {

  private DomainEventTestUtil() {
    // Utility class
  }

  /**
   * Publishes a domain event and waits for asynchronous processing to complete.
   *
   * @param eventPublisher the Spring event publisher
   * @param event          the domain event to publish
   * @param assertion      the assertion to verify after event processing
   * @param timeoutSeconds maximum time to wait for event processing
   */
  public static void publishEventAndWait(
      ApplicationEventPublisher eventPublisher,
      Object event,
      Callable<Void> assertion,
      int timeoutSeconds) {

    eventPublisher.publishEvent(event);

    await()
        .atMost(Duration.ofSeconds(timeoutSeconds))
        .untilAsserted(() -> {
          try {
            assertion.call();
          } catch (Exception e) {
            throw new AssertionError("Event processing assertion failed", e);
          }
        });
  }

  /**
   * Publishes a domain event and waits for asynchronous processing to complete
   * with default timeout of 5 seconds.
   *
   * @param eventPublisher the Spring event publisher
   * @param event          the domain event to publish
   * @param assertion      the assertion to verify after event processing
   */
  public static void publishEventAndWait(
      ApplicationEventPublisher eventPublisher,
      Object event,
      Callable<Void> assertion) {

    publishEventAndWait(eventPublisher, event, assertion, 5);
  }

  /**
   * Waits for a condition to be met, useful for testing eventual consistency
   * in bounded contexts.
   *
   * @param condition      the condition to wait for
   * @param timeoutSeconds maximum time to wait
   */
  public static void waitForCondition(Callable<Boolean> condition, int timeoutSeconds) {
    await()
        .atMost(Duration.ofSeconds(timeoutSeconds))
        .until(condition);
  }

  /**
   * Waits for a condition to be met with default timeout of 5 seconds.
   *
   * @param condition the condition to wait for
   */
  public static void waitForCondition(Callable<Boolean> condition) {
    waitForCondition(condition, 5);
  }
}
