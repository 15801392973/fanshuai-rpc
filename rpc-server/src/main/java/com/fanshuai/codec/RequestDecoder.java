package com.fanshuai.codec;

import com.fanshuai.domain.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RequestDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byteBuf.markReaderIndex();

        //tpc拆包
        int length = byteBuf.readInt();
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data = new byte[length];
        byteBuf.readBytes(data);

        RpcRequest request = RpcCodec.decodeRequest(data);
        list.add(request);
    }
}
