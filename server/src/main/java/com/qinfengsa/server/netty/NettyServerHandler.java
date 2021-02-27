package com.qinfengsa.server.netty;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理网络请求的读写操作
 *
 * @author qinfengsa
 * @date 2021/02/27 22:10
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("msg:{}", msg);
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest) msg;
            log.info("服务端收到信息:{}", request);
        }
        // 返回请求
        RpcResponse response = new RpcResponse();
        response.setCode(200);
        response.setBody(UUID.randomUUID().toString());
        log.debug("服务端发送数据:{}", response);
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Netty IO 错误:{}", cause.getMessage(), cause);
        ctx.close();
    }
}
