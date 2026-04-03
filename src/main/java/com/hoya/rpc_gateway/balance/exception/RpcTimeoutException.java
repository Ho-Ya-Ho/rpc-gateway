package com.hoya.rpc_gateway.balance.exception;

public class RpcTimeoutException extends RuntimeException {

    public RpcTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
