package com.hoya.rpc_gateway.balance.validation;

import com.hoya.rpc_gateway.balance.exception.InvalidWalletAddressException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletAddressValidatorTest {

    private final WalletAddressValidator walletAddressValidator = new WalletAddressValidator();

    @Test
    void validateAcceptsValidEthereumAddress() {
        assertDoesNotThrow(() ->
                walletAddressValidator.validate("0x742d35Cc6634C0532925a3b844Bc454e4438f44e")
        );
    }

    @Test
    void validateRejectsAddressWithoutHexPrefix() {
        assertThrows(InvalidWalletAddressException.class, () ->
                walletAddressValidator.validate("742d35Cc6634C0532925a3b844Bc454e4438f44e")
        );
    }

    @Test
    void validateRejectsAddressWithInvalidLength() {
        assertThrows(InvalidWalletAddressException.class, () ->
                walletAddressValidator.validate("0x1234")
        );
    }

    @Test
    void validateRejectsAddressWithInvalidCharacter() {
        assertThrows(InvalidWalletAddressException.class, () ->
                walletAddressValidator.validate("0x742d35Cc6634C0532925a3b844Bc454e4438f44g")
        );
    }
}
