package com.kvmix.dashboard.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Dashboard.
 *
 * <p>Properties are configured in the {@code application.yml} file.
 */
@ConfigurationProperties(prefix = "kvmix", ignoreUnknownFields = true)
@Setter
public class ApplicationProperties {

}
