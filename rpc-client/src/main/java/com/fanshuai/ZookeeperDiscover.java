package com.fanshuai;

import com.fanshuai.io.IpAndPort;
import com.fanshuai.io.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import util.JSONUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperDiscover {
    private String serviceName;
    private ZooKeeper zooKeeper = null;

    private CountDownLatch latch = new CountDownLatch(1);
    public ZookeeperDiscover(String serviceName, String zkAddr) {
        try {
            this.serviceName = serviceName;

            zooKeeper = new ZooKeeper(zkAddr, 30000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        log.info("connect zookeeper success");
                        System.out.println("connect zookeeper success");
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            log.error("connect zk error, ex={}", e);
            e.printStackTrace();
        }
    }

    public List<IpAndPort> getServerList(ManagedChannel channel) {
        String path = ZkConfig.PATH + serviceName;

        List<IpAndPort> ipAndPorts = new ArrayList<>();
        try {
            List<String> dataList = zooKeeper.getChildren(path, new Watcher()  {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    try {
                        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                            List<String> dataList = zooKeeper.getChildren(path, this);

                            List<IpAndPort> ipAndPorts1 = new ArrayList<>();
                            for (String s : dataList) {
                                String childPath = path + "/" + s;
                                byte[] data = zooKeeper.getData(childPath, false, null);
                                IpAndPort ipAndPort1 = JSONUtil.fromJson(new String(data, StandardCharsets.UTF_8), IpAndPort.class);
                                ipAndPorts1.add(ipAndPort1);
                            }

                            channel.refreshAddrs(ipAndPorts1);
                        }
                    } catch (Exception e) {

                    }
                }
            });

            for (String s : dataList) {
                String childPath = path + "/" + s;
                byte[] data = zooKeeper.getData(childPath, false, null);
                IpAndPort ipAndPort = JSONUtil.fromJson(new String(data, StandardCharsets.UTF_8), IpAndPort.class);
                ipAndPorts.add(ipAndPort);
            }
        } catch (Exception e) {
            log.error("get nodes error, ex={}", e);
            e.printStackTrace();
        }

        return ipAndPorts;
    }
}
