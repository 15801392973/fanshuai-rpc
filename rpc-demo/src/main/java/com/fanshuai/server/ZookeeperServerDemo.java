package com.fanshuai.server;

import com.fanshuai.RpcServer;

public class ZookeeperServerDemo {
    public static void main(String[] args) {
        String zkAddr = "localhost:2181,localhost:2182,localhost:2183";
        RpcServer server = new RpcServer();

        server.bindService(new DemoServiceImpl());

        String serviceName = "rpc-demo";
        int port = Integer.valueOf(args[0]);
        server.register(serviceName, zkAddr, "127.0.0.1", port);
        server.start(port);
    }
}
