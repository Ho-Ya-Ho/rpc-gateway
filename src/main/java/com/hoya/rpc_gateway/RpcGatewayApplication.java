package com.hoya.rpc_gateway;

import com.hoya.rpc_gateway.ratelimit.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(RateLimitProperties.class)
public class RpcGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(RpcGatewayApplication.class, args);
	}

}
