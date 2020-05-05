package com.fanshuai.handler;

import com.fanshuai.Processor;
import com.fanshuai.domain.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    //单线程
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private Processor processor;

    private Map<Channel, Long> heartBeatTimeMap = new ConcurrentHashMap<>();

    public RpcServerHandler(Processor processor) {
        super();

        this.processor = processor;

        //心跳检测
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (MapUtils.isNotEmpty(heartBeatTimeMap)) {
                    for (Channel channel : heartBeatTimeMap.keySet()) {
                        Long time = heartBeatTimeMap.get(channel);
                        if (null == time) {
                            continue;
                        }

                        if (System.currentTimeMillis() - time > ChannelOption.heartBeatFailedCloseSeconds * 1000L) {
                            log.warn("channel hearbeat failed and close channel, channel=" + channel);
                            System.out.println("channel hearbeat failed and close channel, channel=" + channel);

                            heartBeatTimeMap.remove(channel);
                            channel.close();
                        }
                    }
                }
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Frament frament = (Frament) msg;
        MessageType type = MessageType.getMessageType(frament.getMessageType());

        if (null == type) {
            return;
        }

        Channel channel = ctx.channel();
        switch (type) {
            case HEART_BEAT:
                //心跳
                System.out.println("get heartbeat msg=" + frament);
                heartBeatTimeMap.put(channel, System.currentTimeMillis());
                channel.writeAndFlush(frament);
                break;
            case REQUEST:
                //rpc请求
                RpcRequest request = frament.getRpcRequest();
                RpcResponse rpcResponse = processor.processRequest(request);

                Frament responseFrament = Frament.generateRpcResponse(rpcResponse);
                ctx.channel().writeAndFlush(responseFrament);
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        System.out.println("channel inactive, channel=" + ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        cause.printStackTrace();
    }
}
