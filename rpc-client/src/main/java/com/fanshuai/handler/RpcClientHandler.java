package com.fanshuai.handler;

import com.fanshuai.RpcResponseValueContainer;
import com.fanshuai.domain.Frament;
import com.fanshuai.domain.MessageType;
import com.fanshuai.domain.RpcResponse;
import com.fanshuai.io.RpcChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcClientHandler extends SimpleChannelInboundHandler<Frament> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Frament frament) throws Exception {
        MessageType type = MessageType.getMessageType(frament.getMessageType());

        if (null == type) {
            return;
        }
        switch (type) {
            case RESPONSE:
                RpcResponse response = frament.getRpcResponse();
                RpcResponseValueContainer.getInstance().writeRpcResponse(response);
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
