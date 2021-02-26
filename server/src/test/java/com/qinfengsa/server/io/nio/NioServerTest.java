package com.qinfengsa.server.io.nio;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.serialization.HessianSerialization;
import com.qinfengsa.common.serialization.ObjectInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/2/26 17:04
 */
@Slf4j
public class NioServerTest {

    /**
     * 1. 创建ServerSocketChannel， 配置它为非阻塞模式； 2. 绑定监听，配置TCP参数，例如backlog大小； 3.
     * 创建一个独立的I/O线程，用于轮询多路复用器Selector； 4. 创建Selector， 将之前创建的ServerSocketChannel 注册到Selector
     * 上，监听SelectionKey.ACCEPT； 5. 启动I/O线程，在循环体中执行Selector.select() 方法， 轮询就绪的Channel； 6.
     * 当轮询到了处于就绪状态的Channel时，需要对其进行判断，如果是OP_ ACCEPT 状态，说明是新的客户端接入，则调用ServerSocketChannel.accept()
     * 方法接受新的客户端； 7. 设置新接入的客户端链路SocketChannel为非阻塞模式，配置其他的一些TCP参数； 8. 将SocketChannel注册到Selector，
     * 监听OP_ READ操作位； 9. 如果轮询的Channel为OP__READ，则说明SocketChannel中有新的就绪的数据包需要读取，则构造ByteBuffer对象，读取数据包：
     */

    // 端口号
    private int port;

    /** 多路复用器 */
    private Selector selector;

    public NioServerTest(int port) {
        this.port = port;
        try {
            // 打开Channel通道 SelectorProvider.provider().openServerSocketChannel()
            ServerSocketChannel server = ServerSocketChannel.open();
            this.selector = Selector.open();

            // 绑定监听端口
            server.bind(new InetSocketAddress(port));
            // 设置为非阻塞
            server.configureBlocking(false);

            SelectionKey key = server.register(selector, SelectionKey.OP_ACCEPT);
            log.debug("服务端启动-端口号：{}", port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        log.debug("服务端启动成功");
        // 循环监听
        while (true) {
            // 轮询
            int num = selector.select();

            Set<SelectionKey> keySet = selector.selectedKeys();

            Iterator<SelectionKey> iterator = keySet.iterator();
            // 遍历keySet
            while (iterator.hasNext()) {
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

        /*// 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int len = channel.read(buffer);
        if (len > 0) {
            buffer.flip();
            String msg = new String(buffer.array(), 0, len);
            log.debug("服务端收到信息：{}", msg);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 设置成非阻塞 , 设置为写状态
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE);
        }*/

        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int len = channel.read(buffer);
        log.debug("len:{}", len);
        if (len > 0) {
            buffer.flip();
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), 0, len);

            ObjectInput input = new HessianSerialization().deserialize(bis);
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
        String msg = "已收到请求";
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

        channel.write(buffer);
        channel.close();
    }

    public static void main(String[] args) {

        try {
            new NioServerTest(8080).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
