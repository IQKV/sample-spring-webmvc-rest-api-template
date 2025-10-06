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
import com.iqkv.sample.webmvc.dashboard.security.domain.SecurityEventType;
import com.iqkv.sample.webmvc.dashboard.security.domain.ThreatLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository interface for SecurityEvent aggregate.
 */
@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

  Page<SecurityEvent> findByUserId(Long userId, Pageable pageable);

  Page<SecurityEvent> findByEventType(SecurityEventType eventType, Pageable pageable);

  Page<SecurityEvent> findByThreatLevel(ThreatLevel threatLevel, Pageable pageable);

  Page<SecurityEvent> findByResolvedFalse(Pageable pageable);

  @Query("SELECT s FROM SecurityEvent s WHERE s.threatLevel IN ('CRITICAL', 'HIGH') AND s.resolved = false")
  Page<SecurityEvent> findEventsRequiringImmediateAttention(Pageable pageable);

  Page<SecurityEvent> findByIpAddress(String ipAddress, Pageable pageable);

  List<SecurityEvent> findBySessionId(String sessionId);

  Page<SecurityEvent> findByOccurredAtBetween(Instant startTime, Instant endTime, Pageable pageable);

  Page<SecurityEvent> findByUserIdAndOccurredAtAfter(Long userId, Instant since, Pageable pageable);

  long countByEventTypeAndThreatLevelAndOccurredAtBetween(SecurityEventType eventType, ThreatLevel threatLevel, Instant startTime, Instant endTime);

  @Query("SELECT COUNT(s) FROM SecurityEvent s WHERE " +
         "(s.userId = :userId OR (:userId IS NULL AND s.ipAddress = :ipAddress)) AND " +
         "s.eventType = 'FAILED_LOGIN' AND s.occurredAt BETWEEN :startTime AND :endTime")
  long countFailedLoginAttempts(@Param("userId") Long userId,
                                @Param("ipAddress") String ipAddress,
                                @Param("startTime") Instant startTime,
                                @Param("endTime") Instant endTime);

  Page<SecurityEvent> findByUserIdAndEventTypeAndOccurredAtBetween(Long userId, SecurityEventType eventType, Instant startTime, Instant endTime, Pageable pageable);

  long deleteByOccurredAtBefore(Instant cutoffDate);

  Optional<SecurityEvent> findFirstByUserIdOrderByOccurredAtDesc(Long userId);

  @Query("SELECT s FROM SecurityEvent s WHERE s.resolved = false AND s.occurredAt < :maxAge AND s.threatLevel IN ('MEDIUM', 'HIGH', 'CRITICAL')")
  Page<SecurityEvent> findEventsNeedingEscalation(@Param("maxAge") Instant maxAge, Pageable pageable);
}
