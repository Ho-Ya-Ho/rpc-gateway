package com.hoya.rpc_gateway.balance.exception;

public class RpcRequestException extends RuntimeException {

    public RpcRequestException(String message) {
        super(message);
    }

    public RpcRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
