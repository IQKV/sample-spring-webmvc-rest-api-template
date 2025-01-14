/*
 * Copyright 2024 IQKV Team.
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

package com.iqkv.sample.webmvc.dashboard.cucumber.stepdefs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;

import com.iqkv.boot.security.AuthoritiesConstants;
import com.iqkv.sample.webmvc.dashboard.web.rest.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserStepDefs extends StepDefs {

  @Autowired
  private UserResource userResource;

  private MockMvc userResourceMock;

  @Before
  public void setup() {
    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
    User principal = new User("admin", "", true, true, true, true, grantedAuthorities);
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        principal,
        principal.getPassword(),
        principal.getAuthorities()
    );
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    this.userResourceMock = MockMvcBuilders.standaloneSetup(userResource).build();
  }

  @When("I search user {string}")
  public void iSearchUser(String userId) throws Throwable {
    actions = userResourceMock.perform(get("/api/admin/users/" + userId).accept(MediaType.APPLICATION_JSON));
  }

  @Then("the user is found")
  public void theUserIsFound() throws Throwable {
    actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
  }

  @Then("his last name is {string}")
  public void hisLastNameIs(String lastName) throws Throwable {
    actions.andExpect(jsonPath("$.lastName").value(lastName));
  }
}
