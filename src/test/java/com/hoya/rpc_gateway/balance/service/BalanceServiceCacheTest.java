package com.hoya.rpc_gateway.balance.service;

import com.hoya.rpc_gateway.balance.client.EthereumRpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "ethereum.rpc.url=http://localhost",
        "cache.balance.ttl-seconds=60"
})
class BalanceServiceCacheTest {

    @Autowired
    private BalanceService balanceService;

    @MockitoBean
    private EthereumRpcClient ethereumRpcClient;

    @Test
    void getBalanceUsesCacheForRepeatedAddressLookups() {
        String address = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";
        when(ethereumRpcClient.getBalance(address)).thenReturn("0xde0b6b3a7640000");

        balanceService.getBalance(address);
        balanceService.getBalance(address);

        verify(ethereumRpcClient, times(1)).getBalance(address);
    }
}
