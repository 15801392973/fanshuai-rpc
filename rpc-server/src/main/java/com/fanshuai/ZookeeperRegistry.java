package com.fanshuai;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import util.JSONUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperRegistry {
    private String serviceName;


    private ZooKeeper zooKeeper;

    private CountDownLatch latch = new CountDownLatch(1);

    public ZookeeperRegistry(String serviceName, String zkAddr) {
        this.serviceName = serviceName;

        try {
            zooKeeper = new ZooKeeper(zkAddr, 30000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                        log.info("zk conneccted");
                        System.out.println("connect zookeeper success");
                    }
                }
            });

            latch.await();

            String path = ZkConfig.PATH + serviceName;

            //创建基础节点
            Stat stat = zooKeeper.exists(path, false);
            if (stat == null) {
                zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            log.error("connect zk error, ex={}", e);
            e.printStackTrace();
        }
    }

    void register(String ip, int port) {
        try {
            String path = ZkConfig.PATH + serviceName + "/" + ip + "_" + port;
            Stat stat = zooKeeper.exists(path, false);
            if (null == stat) {
                Map<String, Object> map = new HashMap<>();
                map.put("ip", ip);
                map.put("port", port);
                String json = JSONUtil.toJson(map);

                //创建临时节点
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        } catch (Exception e) {
            log.error("create node error, ex={}", e);
        }
    }
}
