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

package com.iqkv.sample.webmvc.dashboard.security.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEvent;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventDomainRepository;
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventType;
import com.iqkv.sample.webmvc.dashboard.security.domain.ThreatLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Repository adapter that implements the domain repository interface
 * and delegates to the JPA repository.
 */
@Component
public class SecurityEventRepositoryAdapter implements SecurityEventDomainRepository {

  private final SecurityEventRepository securityEventRepository;

  public SecurityEventRepositoryAdapter(SecurityEventRepository securityEventRepository) {
    this.securityEventRepository = securityEventRepository;
  }

  @Override
  public SecurityEvent save(SecurityEvent securityEvent) {
    return securityEventRepository.save(securityEvent);
  }

  @Override
  public Optional<SecurityEvent> findById(Long id) {
    return securityEventRepository.findById(id);
  }

  @Override
  public Page<SecurityEvent> findByUserId(Long userId, Pageable pageable) {
    return securityEventRepository.findByUserId(userId, pageable);
  }

  @Override
  public Page<SecurityEvent> findByEventType(SecurityEventType eventType, Pageable pageable) {
    return securityEventRepository.findByEventType(eventType, pageable);
  }

  @Override
  public Page<SecurityEvent> findByThreatLevel(ThreatLevel threatLevel, Pageable pageable) {
    return securityEventRepository.findByThreatLevel(threatLevel, pageable);
  }

  @Override
  public Page<SecurityEvent> findUnresolvedEvents(Pageable pageable) {
    return securityEventRepository.findByResolvedFalse(pageable);
  }

  @Override
  public Page<SecurityEvent> findEventsRequiringImmediateAttention(Pageable pageable) {
    return securityEventRepository.findEventsRequiringImmediateAttention(pageable);
  }

  @Override
  public Page<SecurityEvent> findByIpAddress(String ipAddress, Pageable pageable) {
    return securityEventRepository.findByIpAddress(ipAddress, pageable);
  }

  @Override
  public List<SecurityEvent> findBySessionId(String sessionId) {
    return securityEventRepository.findBySessionId(sessionId);
  }

  @Override
  public Page<SecurityEvent> findByOccurredAtBetween(Instant startTime, Instant endTime, Pageable pageable) {
    return securityEventRepository.findByOccurredAtBetween(startTime, endTime, pageable);
  }

  @Override
  public Page<SecurityEvent> findRecentEventsByUserId(Long userId, Instant since, Pageable pageable) {
    return securityEventRepository.findByUserIdAndOccurredAtAfter(userId, since, pageable);
  }

  @Override
  public long countByEventTypeAndThreatLevelAndOccurredAtBetween(SecurityEventType eventType, ThreatLevel threatLevel, Instant startTime, Instant endTime) {
    return securityEventRepository.countByEventTypeAndThreatLevelAndOccurredAtBetween(eventType, threatLevel, startTime, endTime);
  }

  @Override
  public long countFailedLoginAttempts(Long userId, String ipAddress, Instant startTime, Instant endTime) {
    return securityEventRepository.countFailedLoginAttempts(userId, ipAddress, startTime, endTime);
  }

  @Override
  public Page<SecurityEvent> findByUserIdAndEventTypeAndOccurredAtBetween(Long userId, SecurityEventType eventType, Instant startTime, Instant endTime, Pageable pageable) {
    return securityEventRepository.findByUserIdAndEventTypeAndOccurredAtBetween(userId, eventType, startTime, endTime, pageable);
  }

  @Override
  public long deleteEventsOlderThan(Instant cutoffDate) {
    return securityEventRepository.deleteByOccurredAtBefore(cutoffDate);
  }

  @Override
  public Optional<SecurityEvent> findMostRecentEventByUserId(Long userId) {
    return securityEventRepository.findFirstByUserIdOrderByOccurredAtDesc(userId);
  }

  @Override
  public Page<SecurityEvent> findEventsNeedingEscalation(Instant maxAge, Pageable pageable) {
    return securityEventRepository.findEventsNeedingEscalation(maxAge, pageable);
  }
}