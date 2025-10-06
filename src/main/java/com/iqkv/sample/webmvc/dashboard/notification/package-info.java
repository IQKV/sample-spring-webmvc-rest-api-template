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
 * Notification bounded context.
 * <p>
 * This bounded context handles all notification-related operations including
 * email notifications, SMS notifications, and push notifications.
 * <p>
 * Structure:
 * - domain: Contains notification aggregates and domain services
 * - application: Contains notification services and event handlers
 * - infrastructure: Contains email/SMS service implementations
 * <p>
 * Key concepts:
 * - Notification: Aggregate root for notification management
 * - NotificationType: Value object for different notification types
 * - NotificationChannel: Value object for delivery channels (email, SMS, push)
 */

@org.springframework.modulith.ApplicationModule(
    displayName = "Notification",
    allowedDependencies = {"shared", "config"}
)
package com.iqkv.sample.webmvc.dashboard.notification;
