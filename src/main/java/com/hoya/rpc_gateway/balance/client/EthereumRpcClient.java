package com.hoya.rpc_gateway.balance.client;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EthereumRpcClient {

    private final RestClient.Builder restClientBuilder;
    private final Environment environment;

    private RestClient restClient;

    @PostConstruct
    void initialize() {
        String rpcUrl = environment.getProperty("ethereum.rpc.url");
        if (!StringUtils.hasText(rpcUrl)) {
            throw new IllegalStateException("ethereum.rpc.url must be configured");
        }

        restClient = restClientBuilder
                .baseUrl(rpcUrl)
                .build();
    }

    public String getBalance(String address) {
        JsonNode response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "jsonrpc", "2.0",
                        "method", "eth_getBalance",
                        "params", List.of(address, "latest"),
                        "id", 1
                ))
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("RPC response is empty");
        }

        JsonNode error = response.get("error");
        if (error != null && !error.isNull()) {
            throw new IllegalStateException("RPC request failed: " + error);
        }

        JsonNode result = response.get("result");
        if (result == null || result.isNull()) {
            throw new IllegalStateException("RPC result is missing");
        }

        return result.asText();
    }
}
