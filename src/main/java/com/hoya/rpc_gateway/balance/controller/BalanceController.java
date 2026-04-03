package com.hoya.rpc_gateway.balance.controller;

import com.hoya.rpc_gateway.balance.dto.BalanceResponse;
import com.hoya.rpc_gateway.balance.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balances")
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/{address}")
    public BalanceResponse getBalance(@PathVariable String address) {
        return balanceService.getBalance(address);
    }
}
