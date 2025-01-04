/*
 * Copyright 2024 IQKV.
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

package com.iqkv.sample.webmvc.dashboard.config;

import static com.iqkv.boot.security.SecurityUtils.JWT_ALGORITHM;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.iqkv.boot.security.SecurityProperties;
import com.iqkv.sample.webmvc.dashboard.management.SecurityMetersService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class SecurityJwtConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityJwtConfiguration.class);

  private final SecurityProperties securityProperties;

  public SecurityJwtConfiguration(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public JwtDecoder jwtDecoder(SecurityMetersService metersService) {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();
    return token -> {
      try {
        return jwtDecoder.decode(token);
      } catch (Exception e) {
        if (e.getMessage().contains("Invalid signature")) {
          metersService.trackTokenInvalidSignature();
        } else if (e.getMessage().contains("Jwt expired at")) {
          metersService.trackTokenExpired();
        } else if (
            e.getMessage().contains("Invalid JWT serialization")
            || e.getMessage().contains("Malformed token")
            || e.getMessage().contains("Invalid unsecured/JWS/JWE")
        ) {
          metersService.trackTokenMalformed();
        } else {
          LOG.error("Unknown JWT error {}", e.getMessage());
        }
        throw e;
      }
    };
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Base64.from(securityProperties.getAuthentication().getJwt().getBase64Secret()).decode();
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
  }
}
