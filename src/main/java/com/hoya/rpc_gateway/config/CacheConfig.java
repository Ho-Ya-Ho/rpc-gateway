package com.hoya.rpc_gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${cache.balance.ttl-seconds:30}") long balanceCacheTtlSeconds
    ) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("balance");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofSeconds(balanceCacheTtlSeconds)));
        return cacheManager;
    }
}
