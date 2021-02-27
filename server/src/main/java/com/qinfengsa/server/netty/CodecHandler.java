package com.qinfengsa.server.netty;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.serialization.ObjectInput;
import com.qinfengsa.common.serialization.ObjectOutput;
import com.qinfengsa.common.serialization.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 序列化
 *
 * @author qinfengsa
 * @date 2021/02/27 22:30
 */
public class CodecHandler {

    private final Serialization serialization;

    public CodecHandler(Serialization serialization) {
        this.serialization = serialization;
    }

    public void addHandler(ChannelPipeline pipeline) {

        // 编码器
        pipeline.addLast(
                "encoder",
                new MessageToByteEncoder<Object>() {

                    @Override
                    protected void encode(
                            ChannelHandlerContext channelHandlerContext,
                            Object obj,
                            ByteBuf byteBuf)
                            throws Exception {

                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                            ObjectOutput output = serialization.serialize(bos);
                            output.writeObject(obj);
                            output.flushBuffer();
                            byte[] bytes = bos.toByteArray();
                            byteBuf.writeBytes(bytes);
                        }
                    }
                });
        // 解码器
        pipeline.addLast(
                "decoder",
                new ByteToMessageDecoder() {
                    @Override
                    protected void decode(
                            ChannelHandlerContext channelHandlerContext,
                            ByteBuf byteBuf,
                            List<Object> list)
                            throws Exception {
                        final int len = byteBuf.readableBytes();
                        final byte[] bytes = new byte[len];
                        byteBuf.readBytes(bytes);
                        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
                            ObjectInput input = serialization.deserialize(bis);
                            list.add(input.readObject(RpcRequest.class));
                        }
                    }
                });
    }
}
