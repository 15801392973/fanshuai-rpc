package com.fanshuai.client;

import com.fanshuai.RpcStub;
import com.fanshuai.io.IpAndPort;
import com.fanshuai.io.ManagedChannel;
import com.fanshuai.service.DemoService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZookeeperClientDemo {
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = new ManagedChannel();

        String zkAddr = "localhost:2181,localhost:2182,localhost:2183";
        String serviceName = "rpc-demo";
        List<IpAndPort> serverList = channel.findServerList(serviceName, zkAddr);
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
