package com.fanshuai.domain;

import lombok.Data;

@Data
public class RpcResponse {
    private String requestId;

    private Object value;

    private Throwable throwable;
}
