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

package com.iqkv.boot.security.jwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.iqkv.boot.security.SecurityProperties;
import com.iqkv.sample.webmvc.dashboard.config.ApplicationProperties;
import com.iqkv.sample.webmvc.dashboard.config.SecurityConfiguration;
import com.iqkv.sample.webmvc.dashboard.config.SecurityJwtConfiguration;
import com.iqkv.sample.webmvc.dashboard.config.WebConfigurer;
import com.iqkv.sample.webmvc.dashboard.management.SecurityMetersService;
import com.iqkv.sample.webmvc.dashboard.web.rest.AuthenticateController;
import org.springframework.boot.test.context.SpringBootTest;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    properties = {
        "iqkv.security.authentication.jwt.base64-secret=fd54a45s65fds737b9aafcb3412e07ed99b267f33413274720ddbb7f6c5e64e9f14075f2d7ed041592f0b7657baf8",
        "iqkv.security.authentication.jwt.token-validity-in-seconds=60000",
    },
    classes = {
        ApplicationProperties.class,
        WebConfigurer.class,
        SecurityConfiguration.class,
        SecurityJwtConfiguration.class,
        SecurityMetersService.class,
        AuthenticateController.class,
        JwtAuthenticationTestUtils.class,
        SecurityProperties.class
    }
)
public @interface AuthenticationIntegrationTest {
}
