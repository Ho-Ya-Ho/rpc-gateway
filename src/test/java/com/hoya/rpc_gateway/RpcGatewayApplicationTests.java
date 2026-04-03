package com.hoya.rpc_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "ethereum.rpc.url=http://localhost")
class RpcGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
