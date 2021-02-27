package com.qinfengsa.server.netty;

import com.qinfengsa.server.AbstractServer;
import com.qinfengsa.server.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty 服务端
 *
 * @author qinfengsa
 * @date 2021/02/27 18:11
 */
@Slf4j
public class NettyServer extends AbstractServer {

    CodecHandler codecHandler;

    public NettyServer(ServerConfig config) {

        super(config);
        codecHandler = new CodecHandler(config.getSerialization());
    }

    @Override
    public void start() throws IOException {
        // 配置 Reactor 线程 boss 线程 和 worker 线程
        // boss 线程 负责接收客户端的连接, 连接成功后吧请求转发给worker线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // worker 线程 负责处理 客户端的读写请求
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap 用于启动NIO 服务端
            ServerBootstrap server = new ServerBootstrap();
            // 配置boss现场
            server.group(bossGroup, workerGroup)
                    // 选择 NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 添加childHandler worker 线程负责处理的IO事件
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {

                                    ChannelPipeline pipeline = ch.pipeline();
                                    // 自定义协议解码器 解决 TCP 的 粘包拆包问题
                                    /* 入参有5个，分别解释如下
                                    maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                                    lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                                    lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                                    lengthAdjustment：要添加到长度字段值的补偿值
                                    initialBytesToStrip：从解码帧中去除的第一个字节数
                                    */
                                    pipeline.addLast(
                                            "frameDecoder",
                                            new LengthFieldBasedFrameDecoder(
                                                    Integer.MAX_VALUE, 0, 4, 0, 4));
                                    // 自定义协议编码器
                                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));

                                    // 编码 解码
                                    codecHandler.addHandler(pipeline);

                                    // 自定义Handler, 处理读写请求
                                    pipeline.addLast(new NettyServerHandler());
                                }
                            });

            // bind绑定端口，sync同步等待
            ChannelFuture future = server.bind(getConfig().getPort()).sync();
            if (log.isInfoEnabled()) {
                log.info(
                        "{},服务器开始监听端口:{},等待客户端连接",
                        Thread.currentThread().getName(),
                        getConfig().getPort());
            }
            // 等待端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 线程终止
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
