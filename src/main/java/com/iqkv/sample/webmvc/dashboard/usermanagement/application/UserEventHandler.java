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

package com.iqkv.sample.webmvc.dashboard.usermanagement.application;

import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.PasswordResetRequestedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserActivatedEvent;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event handler for user domain events.
 * Handles cross-cutting concerns like sending emails, logging, etc.
 */
@Component
public class UserEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UserEventHandler.class);

  /**
   * Handles user registration events.
   *
   * @param event the user registered event
   */
  @EventListener
  @Async
  public void handleUserRegistered(UserRegisteredEvent event) {
    LOG.info("User registered: {} with email: {}", event.login(), event.email());

    // Here you would typically:
    // 1. Send activation email
    // 2. Log to audit system
    // 3. Notify other bounded contexts
    // 4. Update analytics/metrics

    // Example: Send activation email (implementation would depend on your mail service)
    // mailService.sendActivationEmail(event.email(), event.activationKey());

    LOG.debug("Activation email would be sent to: {} with key: {}", event.email(), event.activationKey());
  }

  /**
   * Handles user activation events.
   *
   * @param event the user activated event
   */
  @EventListener
  @Async
  public void handleUserActivated(UserActivatedEvent event) {
    LOG.info("User activated: {} with email: {}", event.login(), event.email());

    // Here you would typically:
    // 1. Send welcome email
    // 2. Update user analytics
    // 3. Notify other bounded contexts
    // 4. Initialize user preferences

    // Example: Send welcome email
    // mailService.sendWelcomeEmail(event.email(), event.login());

    LOG.debug("Welcome email would be sent to: {}", event.email());
  }

  /**
   * Handles password reset requested events.
   *
   * @param event the password reset requested event
   */
  @EventListener
  @Async
  public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
    LOG.info("Password reset requested for user with email: {}", event.email());

    // Here you would typically:
    // 1. Send password reset email
    // 2. Log security event
    // 3. Update security metrics

    // Example: Send password reset email
    // mailService.sendPasswordResetEmail(event.email(), event.resetKey());

    LOG.debug("Password reset email would be sent to: {} with key: {}", event.email(), event.resetKey());
  }
}
