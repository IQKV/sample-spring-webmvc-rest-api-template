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

package com.iqkv.sample.webmvc.dashboard.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for monolithic deployment mode.
 * <p>
 * This configuration is activated when the application is deployed
 * as a monolith and provides necessary beans for local service
 * communication.
 */
@Configuration
@ConditionalOnProperty(name = "app.deployment.mode", havingValue = "monolith", matchIfMissing = true)
public class MonolithConfiguration {

  /**
   * RestTemplate for potential external service calls even in monolith mode.
   * <p>
   * This is provided for cases where the monolith needs to communicate
   * with external services or for testing purposes.
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * Configuration properties for monolithic deployment.
   */
  @Bean
  public MonolithProperties monolithProperties() {
    return new MonolithProperties();
  }

  /**
   * Properties for monolithic configuration.
   */
  public static class MonolithProperties {
    private boolean enableAsyncProcessing = true;
    private boolean enableEventDrivenCommunication = true;
    private boolean enableModularTesting = true;
    private int threadPoolSize = 10;

    // Getters and setters
    public boolean isEnableAsyncProcessing() {
      return enableAsyncProcessing;
    }

    public void setEnableAsyncProcessing(boolean enableAsyncProcessing) {
      this.enableAsyncProcessing = enableAsyncProcessing;
    }

    public boolean isEnableEventDrivenCommunication() {
      return enableEventDrivenCommunication;
    }

    public void setEnableEventDrivenCommunication(boolean enableEventDrivenCommunication) {
      this.enableEventDrivenCommunication = enableEventDrivenCommunication;
    }

    public boolean isEnableModularTesting() {
      return enableModularTesting;
    }

    public void setEnableModularTesting(boolean enableModularTesting) {
      this.enableModularTesting = enableModularTesting;
    }

    public int getThreadPoolSize() {
      return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
      this.threadPoolSize = threadPoolSize;
    }
  }
}
