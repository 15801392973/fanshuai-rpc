package com.fanshuai.server;

import com.fanshuai.service.DemoService;
import com.fanshuai.service.Product;

import java.util.HashMap;
import java.util.Map;

public class DemoServiceImpl implements DemoService {
    @Override
    public String greet() {
        return "hello";
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public Map<String, Object> getMap(int code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        return map;
    }

    @Override
    public Product getProduct(int id) {
        Product product = new Product();
        product.setId(id);
        product.setName("name" + id);

        return product;
    }

    @Override
    public void process() {
        System.out.println("process");
    }
}
