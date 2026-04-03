package com.hoya.rpc_gateway.balance.exception;

public class InvalidWalletAddressException extends RuntimeException {

    public InvalidWalletAddressException(String address) {
        super("Invalid Ethereum address: " + address);
    }
}
