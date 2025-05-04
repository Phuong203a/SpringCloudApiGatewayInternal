package com.example.SpringCloudApiGatewayInternal.filter;

import com.example.SpringCloudApiGatewayInternal.utils.Const;
import com.example.SpringCloudApiGatewayInternal.utils.DataUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = request.getId();

        return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer); // safe release

                    String rawBody = new String(bytes, StandardCharsets.UTF_8);
                    byte[] finalBody = bytes;

                    String skipEncrypt = request.getHeaders().getFirst("SKIP-ENCRYPT");

                    if (!"true".equalsIgnoreCase(skipEncrypt) && !rawBody.isEmpty()) {
                        try {
                            JsonNode root = objectMapper.readTree(rawBody);
                            JsonNode dataNode = root.get("data");

                            if (dataNode != null && dataNode.isTextual()) {
                                String encrypted = dataNode.asText();
                                Base64.getDecoder().decode(encrypted); // sanity check
                                log.info("Encrypted Data: {}",encrypted);
                                String decrypted = DataUtils.decrypt(Const.SECRET_AES_KEY, encrypted, "AES");
                                if (decrypted != null) {
                                    finalBody = decrypted.getBytes(StandardCharsets.UTF_8);
                                }
                            } else {
                                return Mono.error(new RuntimeException("Missing 'data' field"));
                            }
                        } catch (Exception e) {
                            return Mono.error(new RuntimeException("Decryption failed", e));
                        }
                    }

                    byte[] bodyToSend = finalBody;
                    ServerHttpRequest modified = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            DataBufferFactory factory = exchange.getResponse().bufferFactory();
                            return Flux.just(factory.wrap(bodyToSend));
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders headers = new HttpHeaders();
                            headers.putAll(super.getHeaders());
                            headers.remove(HttpHeaders.CONTENT_LENGTH);
                            return headers;
                        }
                    };

                    return chain.filter(exchange.mutate().request(modified).build());
                })
                .onErrorResume(e -> {
                    log.error("Request ID: {}, Error: {}", requestId, e.getMessage(), e);
                    exchange.getResponse().setStatusCode(
                            e instanceof PrematureCloseException ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST
                    );
                    return exchange.getResponse().setComplete();
                });
    }
}