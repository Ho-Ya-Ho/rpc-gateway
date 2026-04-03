package com.hoya.rpc_gateway.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        long capacity,
        long refillTokens,
        long refillMinutes
) {
}
