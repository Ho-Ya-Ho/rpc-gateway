package com.hoya.rpc_gateway.internal.dto;

import java.util.Map;

public record CacheEntriesResponse(
        String cacheName,
        Map<Object, Object> entries
) {
}
