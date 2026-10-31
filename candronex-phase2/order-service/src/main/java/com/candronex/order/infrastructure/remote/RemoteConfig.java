package com.candronex.order.infrastructure.remote;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RemoteProperties.class)
public class RemoteConfig { }
