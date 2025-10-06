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
 * User Management bounded context.
 * <p>
 * This bounded context handles user registration, profile management, user lifecycle,
 * and user-related operations following DDD principles.
 * <p>
 * Structure:
 * - domain: Contains aggregates, entities, value objects, domain services, and repository interfaces
 * - application: Contains application services, DTOs, and event handlers
 * - infrastructure: Contains repository implementations and external service adapters
 * - presentation: Contains REST controllers and web-related components
 * <p>
 * Key concepts:
 * - User: Aggregate root representing a user in the system
 * - Authority: Entity representing user permissions/roles
 * - UserProfile: Value object for user personal information
 * - Email: Value object for validated email addresses
 * - Domain events: UserRegisteredEvent, UserActivatedEvent, PasswordResetRequestedEvent
 */

@org.springframework.modulith.ApplicationModule(
    displayName = "User Management",
    allowedDependencies = {"shared", "config"}
)
package com.iqkv.sample.webmvc.dashboard.usermanagement;
