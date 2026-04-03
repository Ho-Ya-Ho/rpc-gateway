package com.hoya.rpc_gateway.internal.dto;

import java.util.List;

public record RateLimitBucketsResponse(
        long capacity,
        long refillTokens,
        long refillMinutes,
        List<RateLimitBucketEntryResponse> buckets
) {
}
