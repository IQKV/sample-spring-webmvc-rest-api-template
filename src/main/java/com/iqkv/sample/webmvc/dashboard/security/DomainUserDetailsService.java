/*
 * Copyright 2025 IQKV Team.
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

package com.iqkv.sample.webmvc.dashboard.security;

import java.util.List;
import java.util.Locale;

import com.iqkv.boot.security.UserNotActivatedException;
import com.iqkv.sample.webmvc.dashboard.domain.Authority;
import com.iqkv.sample.webmvc.dashboard.domain.User;
import com.iqkv.sample.webmvc.dashboard.repository.UserRepository;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

  private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

  private final UserRepository userRepository;

  public DomainUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(final String login) {
    LOG.debug("Authenticating {}", login);

    if (new EmailValidator().isValid(login, null)) {
      return userRepository
          .findOneWithAuthoritiesByEmailIgnoreCase(login)
          .map(user -> createSpringSecurityUser(login, user))
          .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " was not found in the database"));
    }

    String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
    return userRepository
        .findOneWithAuthoritiesByLogin(lowercaseLogin)
        .map(user -> createSpringSecurityUser(lowercaseLogin, user))
        .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));
  }

  private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin, User user) {
    if (!user.isActivated()) {
      throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
    }
    List<SimpleGrantedAuthority> grantedAuthorities = user
        .getAuthorities()
        .stream()
        .map(Authority::getName)
        .map(SimpleGrantedAuthority::new)
        .toList();
    return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(), grantedAuthorities);
  }
}
