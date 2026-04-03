package com.hoya.rpc_gateway.balance.service;

import com.hoya.rpc_gateway.balance.client.EthereumRpcClient;
import com.hoya.rpc_gateway.balance.dto.BalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private static final BigDecimal WEI_PER_ETH = new BigDecimal("1000000000000000000");

    private final EthereumRpcClient ethereumRpcClient;

    public BalanceResponse getBalance(String address) {
        String balanceHex = ethereumRpcClient.getBalance(address);
        BigInteger balanceWei = new BigInteger(stripHexPrefix(balanceHex), 16);
        BigDecimal balanceEth = new BigDecimal(balanceWei)
                .divide(WEI_PER_ETH, 18, RoundingMode.DOWN)
                .stripTrailingZeros();

        return new BalanceResponse(
                address,
                balanceWei.toString(),
                balanceEth.toPlainString()
        );
    }

    private String stripHexPrefix(String hexValue) {
        return hexValue.startsWith("0x") ? hexValue.substring(2) : hexValue;
    }
}
