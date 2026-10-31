package com.candronex.inventory.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("candronex.security")
public record SecurityProperties(String jwkSetUri, String issuer, String audience) { }
