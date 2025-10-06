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

/**
 * Security bounded context.
 * <p>
 * This bounded context handles security-related operations including
 * authentication, authorization, audit logging, and security monitoring.
 * <p>
 * Structure:
 * - domain: Contains security aggregates and policies
 * - application: Contains security services and event handlers
 * - infrastructure: Contains security implementations and audit storage
 * - presentation: Contains security-related APIs and endpoints
 * <p>
 * Key concepts:
 * - SecurityEvent: Aggregate root for security-related events
 * - AuditLog: Aggregate for audit trail management
 * - SecurityPolicy: Value object for security rules
 * - ThreatLevel: Enumeration for security threat levels
 */

@org.springframework.modulith.ApplicationModule(
    displayName = "Security",
    allowedDependencies = {"shared", "config"}
)
package com.iqkv.sample.webmvc.dashboard.security;
