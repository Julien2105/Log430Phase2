package com.candronex.slice.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("candronex.inventory")
public record InventoryProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs,
                                  String tokenUrl, String clientId, String clientSecret) { }
