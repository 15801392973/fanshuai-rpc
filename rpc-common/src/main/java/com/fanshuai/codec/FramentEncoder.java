package com.fanshuai.codec;

import com.fanshuai.domain.Frament;
import com.fanshuai.domain.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class FramentEncoder extends MessageToByteEncoder<Frament> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Frament frament, ByteBuf byteBuf) throws Exception {
        byte[] data = FramentCodec.encode(frament);

        if (null == data) {
            return;
        }

        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
