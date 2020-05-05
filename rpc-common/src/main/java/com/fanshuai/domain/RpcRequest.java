package com.fanshuai.domain;

import lombok.Data;

@Data
public class RpcRequest {
    private String requestId;

    private String className;
    private String methodName;

    private Class[] argTypes;
    private Object[] args;
}
