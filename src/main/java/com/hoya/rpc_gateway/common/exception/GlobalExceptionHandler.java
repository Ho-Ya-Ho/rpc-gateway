package com.hoya.rpc_gateway.common.exception;

import com.hoya.rpc_gateway.balance.exception.InvalidWalletAddressException;
import com.hoya.rpc_gateway.balance.exception.RpcConfigurationException;
import com.hoya.rpc_gateway.balance.exception.RpcRequestException;
import com.hoya.rpc_gateway.balance.exception.RpcTimeoutException;
import com.hoya.rpc_gateway.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidWalletAddressException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWalletAddress(InvalidWalletAddressException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(RpcConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleRpcConfiguration(RpcConfigurationException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(RpcRequestException.class)
    public ResponseEntity<ErrorResponse> handleRpcRequest(RpcRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(RpcTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleRpcTimeout(RpcTimeoutException exception) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
