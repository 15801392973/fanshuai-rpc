package com.fanshuai.io;

import com.fanshuai.RpcResponseValueContainer;
import com.fanshuai.ZookeeperDiscover;
import com.fanshuai.domain.ChannelOption;
import com.fanshuai.domain.RpcRequest;
import com.fanshuai.domain.RpcResponse;
import com.fanshuai.exception.RpcException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManagedChannel {
    private String serverList;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private List<IpAndPort> addr = new ArrayList<>();
    private Map<IpAndPort, RpcChannel> channels = new ConcurrentHashMap<>();

    public void resgisterChannel(RpcChannel channel) {
        IpAndPort ipAndPort = new IpAndPort();
        ipAndPort.setIp(channel.getIp());
        ipAndPort.setPort(channel.getPort());

        //防止重复注册
        for (IpAndPort ipAndPort1 : addr) {
            if (ipAndPort.equals(ipAndPort1)) {
                return;
            }
        }

        synchronized (this) {
            addr.add(ipAndPort);
            channels.put(ipAndPort, channel);

            System.out.println("register channel, addr=" + ipAndPort);
        }
    }

    private void resgisterChannel(IpAndPort ipAndPort) {
        //防止重复注册
        for (IpAndPort ipAndPort1 : addr) {
            if (ipAndPort.equals(ipAndPort1)) {
                return;
            }
        }

        synchronized (this) {
            RpcChannel channel = new RpcChannel(ipAndPort.getIp(), ipAndPort.getPort(), this);
            channel.connect();

            System.out.println("register channel, addr=" + ipAndPort);
        }
    }

    private void removeChannel(IpAndPort ipAndPort) {
        synchronized (this) {
            addr.add(ipAndPort);
            RpcChannel channel = channels.get(ipAndPort);
            if (null != channel) {
                channel.close();
                channels.remove(ipAndPort);
            }

            System.out.println("remove channel, addr=" + ipAndPort);
        }
    }

    public void refreshAddrs(List<IpAndPort> refreshAddr) {
        List<IpAndPort> oldAddr = new ArrayList<>(addr);
        oldAddr.removeAll(refreshAddr);

        List<IpAndPort> newAddr = new ArrayList<>(refreshAddr);
        newAddr.removeAll(addr);

        for (IpAndPort ipAndPort : newAddr) {
            resgisterChannel(ipAndPort);
        }

        for (IpAndPort ipAndPort : oldAddr) {
            removeChannel(ipAndPort);
        }
    }

    private List<IpAndPort> getAddr(String serverList) {
        List<IpAndPort> addrs = new ArrayList<>();
        String[] arr = serverList.split(",");
        if (arr.length > 0) {
            for (String s : arr) {
                IpAndPort ipAndPort = new IpAndPort();
                ipAndPort.setIp(s.split(":")[0]);
                ipAndPort.setPort(Integer.valueOf(s.split(":")[1]));

                addrs.add(ipAndPort);
            }
        }

        return addrs;
    }

    public void init(String serverList) {
        this.serverList = serverList;

        List<IpAndPort> list = getAddr(serverList);
        init(list);
    }

    public void init(List<IpAndPort> addr) {
        for (IpAndPort ipAndPort : addr) {
            RpcChannel rpcChannel = new RpcChannel(ipAndPort.getIp(), ipAndPort.getPort(), this);
            rpcChannel.connect();
        }

        //心跳检测
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (RpcChannel channel : channels.values()) {
                    channel.sendHeartBeat();
                }
            }
        }, 10, ChannelOption.heartBeat, TimeUnit.SECONDS);

        //断线重连
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (RpcChannel channel : channels.values()) {
                    if (!channel.isChannelActive()) {
                        channel.connect();
                    }
                }
            }
        }, 10, ChannelOption.tcpCloseRetrySeconds, TimeUnit.SECONDS);
    }

    public List<IpAndPort> findServerList(String serviceName, String zkAddr) {
        ZookeeperDiscover discover = new ZookeeperDiscover(serviceName, zkAddr);

        return discover.getServerList(this);
    }

    //通过客户端负载均衡获取Channel
    private RpcChannel getChannel() {
        int size = channels.size();
        int index = (int) (Math.random() * size);

        IpAndPort ipAndPort = addr.get(index);
        return channels.get(ipAndPort);
    }

    //rpc调用
    public RpcResponse getResponse(RpcRequest request) throws Exception {
        RpcChannel channel = getChannel();
        if (null == channel) {
            RpcResponse response = new RpcResponse();
            response.setThrowable(new RpcException("服务不可用"));
            return response;
        }

        String requestId = request.getRequestId();

        //开始调用，阻塞当前线程
        RpcResponseValueContainer.getInstance().beginRpcRequest(request, channel);

        //当前线程被唤醒，返回
        return RpcResponseValueContainer.getInstance().getReponse(requestId);
    }


}
