package com.hoya.rpc_gateway.common.exception;

import com.hoya.rpc_gateway.balance.exception.InvalidWalletAddressException;
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
