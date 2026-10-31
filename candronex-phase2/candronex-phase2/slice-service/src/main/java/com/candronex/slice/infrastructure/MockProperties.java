package com.candronex.slice.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("candronex.mock")
public record MockProperties(long latencyMinMs, long latencyMaxMs, double failureRate) { }
