package com.hoya.rpc_gateway.balance.validation;

import com.hoya.rpc_gateway.balance.exception.InvalidWalletAddressException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class WalletAddressValidator {

    private static final Pattern ETHEREUM_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");

    public void validate(String address) {
        if (address == null || !ETHEREUM_ADDRESS_PATTERN.matcher(address).matches()) {
            throw new InvalidWalletAddressException(address);
        }
    }
}
