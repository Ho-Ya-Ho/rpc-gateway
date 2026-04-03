package com.hoya.rpc_gateway.internal.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.hoya.rpc_gateway.internal.dto.CacheEntriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/cache")
public class InternalCacheController {

    private final CacheManager cacheManager;

    @GetMapping("/balance")
    public CacheEntriesResponse getBalanceCacheEntries() {
        org.springframework.cache.Cache springCache = cacheManager.getCache("balance");
        if (springCache == null) {
            throw new IllegalStateException("balance cache is not configured");
        }

        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeCache = (Cache<Object, Object>) springCache.getNativeCache();

        return new CacheEntriesResponse("balance", Map.copyOf(nativeCache.asMap()));
    }
}
