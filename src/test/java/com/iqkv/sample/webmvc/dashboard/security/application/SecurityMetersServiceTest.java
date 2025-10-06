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

package com.iqkv.sample.webmvc.dashboard.security.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SecurityMetersService within the Security bounded context.
 * <p>
 * Tests the application layer's security metrics collection, including
 * authentication failure tracking and security event monitoring.
 */
class SecurityMetersServiceTest {

  private static final String INVALID_TOKENS_METER_NAME = "security.authentication.invalid-tokens";

  private MeterRegistry meterRegistry;
  private SecurityMetricsService securityMetricsService;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    securityMetricsService = new SecurityMetricsService(meterRegistry);
  }

  @Test
  void shouldCreateInvalidTokensCountersByCause() {
    // When - Access counters to trigger creation
    meterRegistry.get(INVALID_TOKENS_METER_NAME).counter();
    meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter();
    meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "unsupported").counter();
    meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "invalid-signature").counter();
    meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "malformed").counter();

    // Then
    Collection<Counter> counters = meterRegistry.find(INVALID_TOKENS_METER_NAME).counters();
    assertThat(counters).hasSize(4); // 4 tagged counters (expired, unsupported, invalid-signature, malformed)
  }

  @Test
  void shouldTrackExpiredTokens() {
    // Given
    Counter expiredCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter();
    assertThat(expiredCounter.count()).isZero();

    // When
    securityMetricsService.trackTokenExpired();

    // Then
    assertThat(expiredCounter.count()).isEqualTo(1);
  }

  @Test
  void shouldTrackUnsupportedTokens() {
    // Given
    Counter unsupportedCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "unsupported").counter();
    assertThat(unsupportedCounter.count()).isZero();

    // When
    securityMetricsService.trackTokenUnsupported();

    // Then
    assertThat(unsupportedCounter.count()).isEqualTo(1);
  }

  @Test
  void shouldTrackInvalidSignatureTokens() {
    // Given
    Counter invalidSignatureCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "invalid-signature").counter();
    assertThat(invalidSignatureCounter.count()).isZero();

    // When
    securityMetricsService.trackTokenInvalidSignature();

    // Then
    assertThat(invalidSignatureCounter.count()).isEqualTo(1);
  }

  @Test
  void shouldTrackMalformedTokens() {
    // Given
    Counter malformedCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "malformed").counter();
    assertThat(malformedCounter.count()).isZero();

    // When
    securityMetricsService.trackTokenMalformed();

    // Then
    assertThat(malformedCounter.count()).isEqualTo(1);
  }

  @Test
  void shouldTrackMultipleTokenFailures() {
    // Given
    Counter expiredCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter();
    Counter malformedCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "malformed").counter();

    // When
    securityMetricsService.trackTokenExpired();
    securityMetricsService.trackTokenExpired();
    securityMetricsService.trackTokenMalformed();

    // Then
    assertThat(expiredCounter.count()).isEqualTo(2);
    assertThat(malformedCounter.count()).isEqualTo(1);
  }

  @Test
  void shouldMaintainSeparateCountersForDifferentCauses() {
    // When
    securityMetricsService.trackTokenExpired();
    securityMetricsService.trackTokenUnsupported();
    securityMetricsService.trackTokenInvalidSignature();
    securityMetricsService.trackTokenMalformed();

    // Then - Each counter should have exactly 1 count
    assertThat(meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter().count()).isEqualTo(1);
    assertThat(meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "unsupported").counter().count()).isEqualTo(1);
    assertThat(meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "invalid-signature").counter().count()).isEqualTo(1);
    assertThat(meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "malformed").counter().count()).isEqualTo(1);
  }

  @Test
  void shouldProvideCorrectMeterNames() {
    // When
    securityMetricsService.trackTokenExpired();

    // Then
    Collection<Counter> counters = meterRegistry.find(INVALID_TOKENS_METER_NAME).counters();
    assertThat(counters).isNotEmpty();

    Counter expiredCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter();
    assertThat(expiredCounter.getId().getName()).isEqualTo(INVALID_TOKENS_METER_NAME);
    assertThat(expiredCounter.getId().getTag("cause")).isEqualTo("expired");
  }

  @Test
  void shouldHandleHighVolumeTracking() {
    // Given
    int numberOfFailures = 1000;

    // When
    for (int i = 0; i < numberOfFailures; i++) {
      securityMetricsService.trackTokenExpired();
    }

    // Then
    Counter expiredCounter = meterRegistry.get(INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter();
    assertThat(expiredCounter.count()).isEqualTo(numberOfFailures);
  }
}
