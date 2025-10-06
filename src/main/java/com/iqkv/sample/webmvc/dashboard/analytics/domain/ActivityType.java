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

package com.iqkv.sample.webmvc.dashboard.analytics.domain;

/**
 * Enumeration representing different types of user activities.
 */
public enum ActivityType {
  LOGIN,
  LOGOUT,
  FAILED_LOGIN,
  REGISTRATION,
  PROFILE_UPDATE,
  PASSWORD_CHANGE,
  EMAIL_VERIFICATION,
  PASSWORD_RESET_REQUEST,
  PASSWORD_RESET_COMPLETE,
  ACCOUNT_ACTIVATION,
  ACCOUNT_DEACTIVATION,
  PERMISSION_CHANGE,
  DATA_EXPORT,
  DATA_IMPORT,
  API_ACCESS,
  SYSTEM_ACCESS,
  PAGE_VIEW,
  FEATURE_USE,
  USER_REGISTERED,
  USER_ACTIVATED
}