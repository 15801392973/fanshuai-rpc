package com.fanshuai.io;

import com.fanshuai.codec.FramentDecoder;
import com.fanshuai.codec.FramentEncoder;
import com.fanshuai.domain.Frament;
import com.fanshuai.domain.RpcRequest;
import com.fanshuai.handler.RpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcChannel {
    private String ip;
    private int port;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private Channel channel;
    private Bootstrap bootstrap = null;

    private ManagedChannel managedChannel;

    public RpcChannel(String ip, int port, ManagedChannel managedChannel) {
        this.ip = ip;
        this.port = port;
        this.managedChannel = managedChannel;
    }

    public boolean isChannelActive() {
        return channel.isActive();
    }

    //发送心跳消息
    public void sendHeartBeat() {
        Frament frament = Frament.generateHeartBeat();
        if (channel.isActive()) {
            channel.writeAndFlush(frament);
            System.out.println("send heartbeat msg=" + frament);
        }
    }

    public boolean sendRpcRequest(RpcRequest request) {
        Frament frament = Frament.generateRpcRequest(request);
        if (channel.isActive()) {
            channel.writeAndFlush(frament);
            return true;
        }

        return false;
    }

    public void close() {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.remove(FramentDecoder.class);
        pipeline.remove(FramentEncoder.class);
        pipeline.remove(RpcClientHandler.class);

        channel.close();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void connect() {

        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        pipeline.addLast(new FramentDecoder())
                                .addLast(new FramentEncoder())
                                .addLast(new RpcClientHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            channel = future.channel();
            System.out.println("connect to server, ip=" + ip + ", port=" + port);

            //注册channel
            if (channel.isActive()) {
                managedChannel.resgisterChannel(RpcChannel.this);
            }
        } catch (Exception e) {
            log.error("connect error, ex={}", e);
            e.printStackTrace();
            executorService.execute(new RetryConnectTask());
        }
    }

    private class RetryConnectTask implements Runnable {
        //连接失败时每5秒 重连一次
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(5);
                connect();
                System.out.println(" retry connect to server, ip=" + ip + ", port=" + port);

                if (channel.isActive()) {
                    managedChannel.resgisterChannel(RpcChannel.this);
                }
            } catch (Exception e) {
                log.error("connect error, ex={}", e);
                e.printStackTrace();
            }
        }
    }
}
