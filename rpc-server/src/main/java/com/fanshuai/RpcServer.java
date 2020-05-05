package com.fanshuai;

import com.fanshuai.codec.FramentDecoder;
import com.fanshuai.codec.FramentEncoder;
import com.fanshuai.handler.RpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;

public class RpcServer {
    private Processor processor = new Processor();
    private ZookeeperRegistry registry = null;

    public void bindService(Object service) {
        processor.addServiceMap(service);
    }

    public void bindServices(List<Object> services) {
        processor.addServiceMap(services);
    }

    public void start(int port) {
        start(10, port);
    }

    public void start(int concurrency, int port) {
        ServerBootstrap bootstrap = new ServerBootstrap();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(concurrency);

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            //解码
                            pipeline.addLast(new FramentDecoder());
                            //编码
                            pipeline.addLast(new FramentEncoder());
                            //业务处理
                            pipeline.addLast(new RpcServerHandler(processor));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();

            System.out.println("rpc server started");
            future.channel().closeFuture().sync();

            System.out.println("rpc server stoped");
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void register(String serviceName, String zkAddr,  String ip, int port) {
        registry = new ZookeeperRegistry(serviceName, zkAddr);
        registry.register(ip, port);
    }
}
