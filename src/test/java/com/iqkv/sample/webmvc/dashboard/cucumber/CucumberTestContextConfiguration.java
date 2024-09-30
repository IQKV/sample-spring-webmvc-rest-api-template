package com.iqkv.sample.webmvc.dashboard.cucumber;

import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
public class CucumberTestContextConfiguration {
}
