package com.hoya.rpc_gateway.balance.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.hoya.rpc_gateway.balance.exception.RpcConfigurationException;
import com.hoya.rpc_gateway.balance.exception.RpcRequestException;
import com.hoya.rpc_gateway.balance.exception.RpcTimeoutException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
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
            throw new RpcConfigurationException("ethereum.rpc.url must be configured");
        }

        int connectTimeoutMillis = environment.getProperty("ethereum.rpc.connect-timeout-ms", Integer.class, 2_000);
        int readTimeoutMillis = environment.getProperty("ethereum.rpc.read-timeout-ms", Integer.class, 3_000);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);

        restClient = restClientBuilder
                .baseUrl(rpcUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String getBalance(String address) {
        try {
            JsonNode response = requestBalance(address);
            validateResponse(response);
            return extractResult(response);
        } catch (ResourceAccessException exception) {
            throw translateResourceAccessException(exception);
        } catch (RestClientResponseException exception) {
            throw new RpcRequestException(
                    "RPC provider returned HTTP " + exception.getStatusCode().value(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new RpcRequestException("Unexpected RPC client error", exception);
        }
    }

    private JsonNode requestBalance(String address) {
        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBalanceRequest(address))
                .retrieve()
                .body(JsonNode.class);
    }

    private Map<String, Object> createBalanceRequest(String address) {
        return Map.of(
                "jsonrpc", "2.0",
                "method", "eth_getBalance",
                "params", List.of(address, "latest"),
                "id", 1
        );
    }

    private void validateResponse(JsonNode response) {
        if (response == null) {
            throw new RpcRequestException("RPC response is empty");
        }

        JsonNode error = response.get("error");
        if (error != null && !error.isNull()) {
            throw new RpcRequestException("RPC request failed: " + error);
        }

        JsonNode result = response.get("result");
        if (result == null || result.isNull()) {
            throw new RpcRequestException("RPC result is missing");
        }
    }

    private String extractResult(JsonNode response) {
        return response.get("result").asText();
    }

    private RuntimeException translateResourceAccessException(ResourceAccessException exception) {
        if (hasSocketTimeout(exception)) {
            return new RpcTimeoutException("RPC request timed out", exception);
        }
        return new RpcRequestException("Failed to access RPC provider", exception);
    }

    private boolean hasSocketTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
