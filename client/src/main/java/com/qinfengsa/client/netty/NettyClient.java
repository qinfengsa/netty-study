package com.qinfengsa.client.netty;

import com.qinfengsa.client.AbstractClient;
import com.qinfengsa.client.ClientConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty 客户端
 *
 * @author qinfengsa
 * @date 2021/02/27 18:13
 */
@Slf4j
public class NettyClient extends AbstractClient {

    static ExecutorService pool = Executors.newFixedThreadPool(10);

    CodecHandler codecHandler;

    public NettyClient(ClientConfig config) {
        super(config);
        codecHandler = new CodecHandler(config.getSerialization());
    }

    public void connect() {
        // 客户端 IO 线程组
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();

            client.group(clientGroup)
                    // 选择 NioSocketChannel
                    .channel(NioSocketChannel.class)
                    // TCP 参数
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(
                            new ChannelInitializer<>() {
                                @Override
                                protected void initChannel(Channel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    // 自定义协议解码器
                                    pipeline.addLast(
                                            "frameDecoder",
                                            new LengthFieldBasedFrameDecoder(
                                                    Integer.MAX_VALUE, 0, 4, 0, 4));
                                    // 自定义协议编码器
                                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));

                                    // 编码 解码
                                    codecHandler.addHandler(pipeline);

                                    pipeline.addLast(new NettyClientHandler());
                                }
                            });
            // connect 发起连接请求 sync 等待连接
            ChannelFuture future =
                    client.connect(getConfig().getAddr(), getConfig().getPort()).sync();
            log.debug("{},客户端发起异步连接......", Thread.currentThread().getName());
            // 等待连接关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clientGroup.shutdownGracefully();
        }
    }

    @Override
    public void send() {
        for (int i = 0; i < 2; i++) {
            pool.execute(this::connect);
        }
    }
}
