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
