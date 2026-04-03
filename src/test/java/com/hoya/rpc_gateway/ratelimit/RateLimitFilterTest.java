package com.hoya.rpc_gateway.ratelimit;

import com.hoya.rpc_gateway.balance.client.EthereumRpcClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "ethereum.rpc.url=http://localhost",
        "rate-limit.capacity=1",
        "rate-limit.refill-tokens=1",
        "rate-limit.refill-minutes=1"
})
@AutoConfigureMockMvc
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EthereumRpcClient ethereumRpcClient;

    @Test
    void rejectsRequestsThatExceedConfiguredLimit() throws Exception {
        String address = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";
        when(ethereumRpcClient.getBalance(address)).thenReturn("0xde0b6b3a7640000");

        mockMvc.perform(get("/api/balances/{address}", address)
                        .header("X-Forwarded-For", "203.0.113.10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/balances/{address}", address)
                        .header("X-Forwarded-For", "203.0.113.10"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }
}
