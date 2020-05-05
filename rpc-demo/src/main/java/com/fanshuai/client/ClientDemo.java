package com.fanshuai.client;

import com.fanshuai.RpcStub;
import com.fanshuai.io.ManagedChannel;
import com.fanshuai.service.DemoService;

import java.util.concurrent.CountDownLatch;

public class ClientDemo {
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        String serverList = "127.0.0.1:5050";

        ManagedChannel channel = new ManagedChannel();
        channel.init(serverList);

        //rpc client stub
        RpcStub stub = new RpcStub(channel);
        DemoService demoService = stub.generateProxyService(DemoService.class);

        System.out.println(demoService.add(1, 2));
        System.out.println(demoService.greet());
        System.out.println(demoService.getMap(11, "11"));
        System.out.println(demoService.getProduct(1));
        demoService.process();

        latch.await();
    }
}
