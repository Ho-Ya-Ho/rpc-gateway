package com.hoya.rpc_gateway.internal.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.hoya.rpc_gateway.internal.dto.RateLimitBucketEntryResponse;
import com.hoya.rpc_gateway.internal.dto.RateLimitBucketsResponse;
import com.hoya.rpc_gateway.ratelimit.RateLimitProperties;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rate-limit")
public class InternalRateLimitController {

    private final Cache<String, Bucket> rateLimitBucketCache;
    private final RateLimitProperties rateLimitProperties;

    @GetMapping("/buckets")
    public RateLimitBucketsResponse getRateLimitBuckets() {
        List<RateLimitBucketEntryResponse> buckets = rateLimitBucketCache.asMap().entrySet().stream()
                .map(entry -> new RateLimitBucketEntryResponse(
                        entry.getKey(),
                        entry.getValue().getAvailableTokens()
                ))
                .sorted(Comparator.comparing(RateLimitBucketEntryResponse::clientIp))
                .toList();

        return new RateLimitBucketsResponse(
                rateLimitProperties.capacity(),
                rateLimitProperties.refillTokens(),
                rateLimitProperties.refillMinutes(),
                buckets
        );
    }
}
