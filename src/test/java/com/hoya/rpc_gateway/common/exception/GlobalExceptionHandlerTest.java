package com.hoya.rpc_gateway.common.exception;

import com.hoya.rpc_gateway.balance.exception.RpcConfigurationException;
import com.hoya.rpc_gateway.balance.exception.RpcRequestException;
import com.hoya.rpc_gateway.balance.exception.RpcTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleRpcConfigurationReturnsInternalServerError() {
        var response = globalExceptionHandler.handleRpcConfiguration(
                new RpcConfigurationException("ethereum.rpc.url must be configured")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ethereum.rpc.url must be configured", response.getBody().message());
    }

    @Test
    void handleRpcRequestReturnsBadGateway() {
        var response = globalExceptionHandler.handleRpcRequest(
                new RpcRequestException("Failed to access RPC provider")
        );

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Failed to access RPC provider", response.getBody().message());
    }

    @Test
    void handleRpcTimeoutReturnsGatewayTimeout() {
        var response = globalExceptionHandler.handleRpcTimeout(
                new RpcTimeoutException("RPC request timed out", new SocketTimeoutException())
        );

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());
        assertEquals("RPC request timed out", response.getBody().message());
    }
}
