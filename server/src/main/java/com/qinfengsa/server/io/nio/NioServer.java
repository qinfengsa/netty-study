package com.qinfengsa.server.io.nio;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import com.qinfengsa.common.serialization.ObjectInput;
import com.qinfengsa.common.serialization.ObjectOutput;
import com.qinfengsa.common.serialization.Serialization;
import com.qinfengsa.server.AbstractServer;
import com.qinfengsa.server.ServerConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * NIO 服务端
 *
 * @author qinfengsa
 * @date 2021/2/26 15:06
 */
@Slf4j
public class NioServer extends AbstractServer {

    // 多路复用器
    private Selector selector;

    private final Serialization serialization;

    public NioServer(ServerConfig config) {
        super(config);
        serialization = config.getSerialization();
        try {
            // 启动 ServerSocketChannel
            ServerSocketChannel server = ServerSocketChannel.open();
            // 启动 selector
            selector = Selector.open();
            // 绑定端口

            server.bind(new InetSocketAddress(getConfig().getPort()));
            // 设置为非阻塞
            server.configureBlocking(false);
            // 注册 accept 事件
            server.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void start() throws IOException {

        log.debug("服务端启动-端口号:{}", getConfig().getPort());
        while (true) {
            // 轮询
            int num = selector.select();
            if (num == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            // 遍历keySet
            for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext(); ) {
                SelectionKey key = iterator.next();
                // 删除已有key
                iterator.remove();
                // 客户端请求
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 建立连接
                    SocketChannel channel = server.accept();
                    // 设置成非阻塞
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int len = channel.read(buffer);
        if (len > 0) {
            buffer.flip();
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), 0, len);

            ObjectInput input = serialization.deserialize(bis);
            RpcRequest request = input.readObject(RpcRequest.class);
            log.info("服务端收到信息:{}", request);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 设置成非阻塞 , 设置为写状态
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE);
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        RpcResponse response = new RpcResponse();
        response.setCode(200);
        response.setBody(UUID.randomUUID().toString());
        log.debug("服务端发送数据:{}", response);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput output = serialization.serialize(bos);
            output.writeObject(response);
            output.flushBuffer();
            byte[] bytes = bos.toByteArray();
            channel.write(ByteBuffer.wrap(bytes));
        }
        channel.close();
    }
}
