package com.fanshuai.codec;

import com.fanshuai.domain.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse response, ByteBuf byteBuf) throws Exception {
        byte[] data = RpcCodec.encodeResponse(response);

        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
