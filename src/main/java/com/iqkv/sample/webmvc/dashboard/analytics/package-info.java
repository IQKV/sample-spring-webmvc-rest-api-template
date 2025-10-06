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
 * Analytics bounded context.
 * <p>
 * This bounded context handles user behavior tracking, metrics collection,
 * and analytics reporting across the application.
 * <p>
 * Structure:
 * - domain: Contains analytics aggregates and value objects
 * - application: Contains analytics services and event handlers
 * - infrastructure: Contains metrics storage and reporting implementations
 * - presentation: Contains analytics dashboards and reporting APIs
 * <p>
 * Key concepts:
 * - UserActivity: Aggregate root for tracking user actions
 * - Metric: Value object for various measurements
 * - Report: Aggregate for analytics reports
 * - ActivityType: Enumeration for different user activities
 */

@org.springframework.modulith.ApplicationModule(
    displayName = "Analytics",
    allowedDependencies = {"shared", "config"}
)
package com.iqkv.sample.webmvc.dashboard.analytics;
