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
 * Dashboard Application - Modular Monolith with DDD Bounded Contexts.
 * <p>
 * This package contains the main application and serves as the root for
 * Spring Modulith module detection. Each subdirectory represents a
 * bounded context that is automatically detected as a module.
 * <p>
 * Bounded Contexts (Modules):
 * - usermanagement: User Management bounded context
 * - security: Security bounded context
 * - notification: Notification bounded context
 * - analytics: Analytics bounded context
 * - shared: Shared kernel for common concerns
 * - config: Application-wide configuration
 */

@org.springframework.modulith.ApplicationModule
package com.iqkv.sample.webmvc.dashboard;
