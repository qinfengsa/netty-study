package com.qinfengsa.client.io.nio;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.serialization.HessianSerialization;
import com.qinfengsa.common.serialization.ObjectOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/2/26 17:05
 */
@Slf4j
public class NioClientTest {
    static ExecutorService pool = Executors.newFixedThreadPool(2);

    // 服务地址
    private final InetSocketAddress serverAdrress = new InetSocketAddress("localhost", 8080);

    private Selector selector;

    private SocketChannel client;

    public NioClientTest() throws IOException {

        selector = Selector.open();
        client = SocketChannel.open(serverAdrress);
        client.configureBlocking(false);

        /* String name = UUID.randomUUID().toString();
                log.debug("发送数据：{}", name);
                ByteBuffer buffer = ByteBuffer.wrap(name.getBytes());
                client.write(buffer);
        */
        // 写数据
        RpcRequest request = RpcRequest.getDefaultRequest(0);

        log.debug("发送数据:{}", request);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput output = new HessianSerialization().serialize(bos);
            output.writeObject(request);
            output.flushBuffer();
            byte[] bytes = bos.toByteArray();
            client.write(ByteBuffer.wrap(bytes));
        }
        // 注册
        client.register(selector, SelectionKey.OP_READ);
    }

    public void start() {
        // pool.execute(new Writer());
        pool.execute(new Reader());
    }

    class Writer implements Runnable {
        @Override
        public void run() {
            try {

                String name = UUID.randomUUID().toString();
                log.debug("发送数据：{}", name);
                ByteBuffer buffer = ByteBuffer.wrap(name.getBytes());
                client.write(buffer);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Reader implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    int readChannels = selector.select();
                    if (readChannels == 0) {
                        continue;
                    }
                    Set<SelectionKey> keySet = selector.selectedKeys();

                    Iterator<SelectionKey> keys = keySet.iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();

                        if (key.isReadable()) {
                            doRead(key);
                        }
                        keys.remove();
                    }
                }
            } catch (IOException e) {

            }
        }

        private void doRead(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int len = channel.read(buffer);
            if (len > 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, len);
                log.debug("收取服务器信息：{}", msg);
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NioClientTest().start();
    }
}
