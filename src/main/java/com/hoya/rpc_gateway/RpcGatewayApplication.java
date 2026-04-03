package com.hoya.rpc_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RpcGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(RpcGatewayApplication.class, args);
	}

}
