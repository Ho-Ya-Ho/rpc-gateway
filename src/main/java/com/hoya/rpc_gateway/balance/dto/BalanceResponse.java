package com.hoya.rpc_gateway.balance.dto;

public record BalanceResponse(
        String address,
        String balanceWei,
        String balanceEth
) {
}
