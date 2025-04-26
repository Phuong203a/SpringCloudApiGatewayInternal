package com.example.SpringCloudApiGatewayInternal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseRequestDTO {
    private String requestId = UUID.randomUUID().toString();
    private long timestamp = System.currentTimeMillis();
    private Long userId;
    private String content;
    private String nonce;
    private String requestUri;
    private String signature;
    private String apiTimeStamp;
}
