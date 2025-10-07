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

package com.iqkv.sample.webmvc.dashboard.user.service;

import java.security.SecureRandom;

/**
 * Utility class for generating random Strings.
 */
public final class RandomUtil {

  private static final int DEF_COUNT = 20;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  static {
    SECURE_RANDOM.nextBytes(new byte[64]);
  }

  private RandomUtil() {
    // Utility class
  }

  /**
   * Generate a password.
   *
   * @return the generated password.
   */
  public static String generatePassword() {
    return generateRandomAlphanumericString();
  }

  /**
   * Generate an activation key.
   *
   * @return the generated activation key.
   */
  public static String generateActivationKey() {
    return generateRandomAlphanumericString();
  }

  /**
   * Generate a reset key.
   *
   * @return the generated reset key.
   */
  public static String generateResetKey() {
    return generateRandomAlphanumericString();
  }

  /**
   * Generate a random alphanumeric string.
   *
   * @return the generated string.
   */
  public static String generateRandomAlphanumericString() {
    return generateRandomAlphanumericString(DEF_COUNT);
  }

  private static String generateRandomAlphanumericString(int count) {
    final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
    }
    return sb.toString();
  }
}