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

package com.iqkv.sample.webmvc.dashboard.service.dto;

import java.io.Serializable;
import java.util.Objects;

import com.iqkv.sample.webmvc.dashboard.domain.User;

/**
 * A DTO representing a user, with only the public attributes.
 */
public class UserDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String login;

  public UserDTO() {
    // Empty constructor needed for Jackson.
  }

  public UserDTO(User user) {
    this.id = user.getId();
    // Customize it here if you need, or not, firstName/lastName/etc
    this.login = user.getLogin();
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UserDTO userDTO = (UserDTO) o;
    if (userDTO.getId() == null || getId() == null) {
      return false;
    }

    return Objects.equals(getId(), userDTO.getId()) && Objects.equals(getLogin(), userDTO.getLogin());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getLogin());
  }

  // prettier-ignore
  @Override
  public String toString() {
    return "UserDTO{" +
           "id='" + id + '\'' +
           ", login='" + login + '\'' +
           "}";
  }
}
