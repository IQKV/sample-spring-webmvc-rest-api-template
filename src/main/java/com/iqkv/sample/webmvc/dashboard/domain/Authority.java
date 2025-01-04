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

package com.iqkv.sample.webmvc.dashboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.domain.Persistable;

/**
 * A Authority.
 */
@Entity
@Table(name = "iqkv_authority")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties(value = {"new", "id"})
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Authority implements Serializable, Persistable<String> {

  @Serial
  private static final long serialVersionUID = 1L;

  @NotNull
  @Size(max = 50)
  @Id
  @Column(name = "name", length = 50, nullable = false)
  private String name;

  @Transient
  private boolean isPersisted;


  public String getName() {
    return this.name;
  }

  public Authority name(String name) {
    this.setName(name);
    return this;
  }

  public void setName(String name) {
    this.name = name;
  }

  @PostLoad
  @PostPersist
  public void updateEntityState() {
    this.setIsPersisted();
  }

  @Override
  public String getId() {
    return this.name;
  }

  @Transient
  @Override
  public boolean isNew() {
    return !this.isPersisted;
  }

  public Authority setIsPersisted() {
    this.isPersisted = true;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Authority)) {
      return false;
    }
    return getName() != null && getName().equals(((Authority) o).getName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName());
  }

  // prettier-ignore
  @Override
  public String toString() {
    return "Authority{" +
           "name=" + getName() +
           "}";
  }
}
