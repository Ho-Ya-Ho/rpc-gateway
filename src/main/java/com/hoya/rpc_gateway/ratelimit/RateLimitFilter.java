package com.hoya.rpc_gateway.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.hoya.rpc_gateway.common.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> rateLimitBucketCache;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/balances");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        Bucket bucket = rateLimitBucketCache.get(clientIp, key -> newBucket());

        if (bucket == null || !bucket.tryConsume(1)) {
            writeRateLimitExceededResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 프록시 환경을 고려해서 X-Forwarded-For 헤더가 있으면 그 첫 번째 IP를 쓰고, 없으면 request.getRemoteAddr()를 씁니다.
     * @param request
     * @return
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket() {
        Refill refill = Refill.intervally(
                rateLimitProperties.refillTokens(),
                Duration.ofMinutes(rateLimitProperties.refillMinutes())
        );
        Bandwidth limit = Bandwidth.classic(rateLimitProperties.capacity(), refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * bucket이 허용량을 초과했을 떄, 429 반환하는 메서드
     * @param response
     * @throws IOException
     */
    private void writeRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse("Too many requests. Please try again later.")
        );
    }
}
