package com.qinfengsa.client.io.nio;

import com.qinfengsa.client.AbstractClient;
import com.qinfengsa.client.ClientConfig;
import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import com.qinfengsa.common.serialization.ObjectInput;
import com.qinfengsa.common.serialization.ObjectOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * NIO 客户端
 *
 * @author qinfengsa
 * @date 2021/2/26 15:05
 */
@Slf4j
public class NioClient extends AbstractClient {

    static ExecutorService pool = Executors.newFixedThreadPool(10);

    // 服务地址
    private final InetSocketAddress serverAddress;

    public NioClient(ClientConfig config) {
        super(config);
        serverAddress = new InetSocketAddress(config.getAddr(), config.getPort());
    }

    @Override
    public void send() {
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            pool.execute(
                    () -> {
                        try (SocketChannel socketChannel = SocketChannel.open(serverAddress);
                                Selector selector = Selector.open(); ) {
                            // 设置非阻塞式
                            socketChannel.configureBlocking(false);
                            // 写数据
                            RpcRequest request = RpcRequest.getDefaultRequest(idx);

                            log.debug("发送数据:{}", request);
                            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                                ObjectOutput output = getConfig().getSerialization().serialize(bos);
                                output.writeObject(request);
                                output.flushBuffer();
                                byte[] bytes = bos.toByteArray();
                                socketChannel.write(ByteBuffer.wrap(bytes));
                            }

                            socketChannel.register(selector, SelectionKey.OP_READ);
                            while (true) {
                                int num = selector.select();
                                if (num == 0) {
                                    continue;
                                }
                                Set<SelectionKey> keys = selector.selectedKeys();
                                for (Iterator<SelectionKey> iterator = keys.iterator();
                                        iterator.hasNext(); ) {
                                    SelectionKey key = iterator.next();

                                    if (key.isReadable()) {
                                        read(key);
                                    }
                                    iterator.remove();
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
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

            ObjectInput input = getConfig().getSerialization().deserialize(bis);
            RpcResponse response = input.readObject(RpcResponse.class);
            log.debug("收到服务器响应:{}", response);
        }
        channel.close();
    }
}
