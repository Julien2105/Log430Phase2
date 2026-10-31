package com.candronex.order.infrastructure.remote;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("candronex.remote")
public record RemoteProperties(Target drone, Target catalog, Target slice) {

    public record Target(String baseUrl, int connectTimeoutMs, int readTimeoutMs) { }
}
