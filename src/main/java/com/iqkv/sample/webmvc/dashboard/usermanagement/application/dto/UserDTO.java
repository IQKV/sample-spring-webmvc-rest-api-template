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

package com.iqkv.sample.webmvc.dashboard.usermanagement.application.dto;

import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;

/**
 * DTO for public user information.
 */
public class UserDTO {

  private Long id;
  private String login;
  private String firstName;
  private String lastName;
  private String email;
  private String imageUrl;
  private String langKey;

  public UserDTO() {
    // Empty constructor needed for Jackson.
  }

  public UserDTO(User user) {
    this.id = user.getId();
    this.login = user.getLogin();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.imageUrl = user.getImageUrl();
    this.langKey = user.getLangKey();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getLangKey() {
    return langKey;
  }

  public void setLangKey(String langKey) {
    this.langKey = langKey;
  }

  @Override
  public String toString() {
    return "UserDTO{" +
           "id=" + id +
           ", login='" + login + '\'' +
           ", firstName='" + firstName + '\'' +
           ", lastName='" + lastName + '\'' +
           ", email='" + email + '\'' +
           ", imageUrl='" + imageUrl + '\'' +
           ", langKey='" + langKey + '\'' +
           '}';
  }
}