package com.fanshuai.server;

import com.fanshuai.RpcServer;

public class ServerDemo {
    public static void main(String[] args) {
        RpcServer server = new RpcServer();

        server.bindService(new DemoServiceImpl());
        server.start(5050);
    }
}
