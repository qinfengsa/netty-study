package com.qinfengsa.client.netty;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/02/27 22:53
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 写数据
        RpcRequest request = RpcRequest.getDefaultRequest(10);

        log.debug("发送数据:{}", request);

        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("msg:{}", msg);
        if (msg instanceof RpcResponse) {
            RpcResponse response = (RpcResponse) msg;
            log.debug("收到服务器响应:{}", response);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Netty IO 错误:{}", cause.getMessage(), cause);
        ctx.close();
    }
}
