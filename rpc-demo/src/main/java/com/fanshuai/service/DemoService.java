package com.fanshuai.service;

import com.fanshuai.annotation.RpcMethod;

import java.util.Map;

public interface DemoService {
    @RpcMethod
    String greet();

    @RpcMethod
    int add(int a, int b);

    @RpcMethod
    Map<String, Object> getMap(int code, String message);

    @RpcMethod
    Product getProduct(int id);

    @RpcMethod
    void process();
}
