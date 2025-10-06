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

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for microservices deployment mode.
 * <p>
 * This configuration is activated when the application is deployed
 * in microservices mode and provides necessary beans for remote
 * service communication.
 */
@Configuration
@ConditionalOnProperty(name = "app.deployment.mode", havingValue = "microservices")
public class MicroservicesConfiguration {

  /**
   * RestTemplate for inter-service communication.
   * <p>
   * In a production environment, this would be enhanced with:
   * - Load balancing (Ribbon, Spring Cloud LoadBalancer)
   * - Circuit breaker (Hystrix, Resilience4j)
   * - Service discovery integration
   * - Authentication/authorization
   * - Retry mechanisms
   * - Monitoring and tracing
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .build();
  }

  /**
   * Configuration properties for microservices.
   */
  @Bean
  public MicroservicesProperties microservicesProperties() {
    return new MicroservicesProperties();
  }

  /**
   * Properties for microservices configuration.
   */
  public static class MicroservicesProperties {
    private boolean enableCircuitBreaker = true;
    private boolean enableRetry = true;
    private boolean enableLoadBalancing = true;
    private int maxRetryAttempts = 3;
    private Duration retryDelay = Duration.ofMillis(500);
    private Duration circuitBreakerTimeout = Duration.ofSeconds(10);

    // Getters and setters
    public boolean isEnableCircuitBreaker() {
      return enableCircuitBreaker;
    }

    public void setEnableCircuitBreaker(boolean enableCircuitBreaker) {
      this.enableCircuitBreaker = enableCircuitBreaker;
    }

    public boolean isEnableRetry() {
      return enableRetry;
    }

    public void setEnableRetry(boolean enableRetry) {
      this.enableRetry = enableRetry;
    }

    public boolean isEnableLoadBalancing() {
      return enableLoadBalancing;
    }

    public void setEnableLoadBalancing(boolean enableLoadBalancing) {
      this.enableLoadBalancing = enableLoadBalancing;
    }

    public int getMaxRetryAttempts() {
      return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
      this.maxRetryAttempts = maxRetryAttempts;
    }

    public Duration getRetryDelay() {
      return retryDelay;
    }

    public void setRetryDelay(Duration retryDelay) {
      this.retryDelay = retryDelay;
    }

    public Duration getCircuitBreakerTimeout() {
      return circuitBreakerTimeout;
    }

    public void setCircuitBreakerTimeout(Duration circuitBreakerTimeout) {
      this.circuitBreakerTimeout = circuitBreakerTimeout;
    }
  }
}
