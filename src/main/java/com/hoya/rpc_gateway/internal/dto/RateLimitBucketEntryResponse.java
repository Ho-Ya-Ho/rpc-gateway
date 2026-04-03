package com.hoya.rpc_gateway.internal.dto;

public record RateLimitBucketEntryResponse(
        String clientIp,
        long availableTokens
) {
}
