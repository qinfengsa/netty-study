package com.qinfengsa.server.io.aio;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import com.qinfengsa.common.serialization.ObjectInput;
import com.qinfengsa.common.serialization.ObjectOutput;
import com.qinfengsa.server.AbstractServer;
import com.qinfengsa.server.ServerConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * AIO 服务端
 *
 * @author qinfengsa
 * @date 2021/2/26 18:09
 */
@Slf4j
public class AioServer extends AbstractServer {

    private AsynchronousServerSocketChannel server;

    static ExecutorService pool = Executors.newFixedThreadPool(5);

    public AioServer(ServerConfig config) {
        super(config);
    }

    @Override
    public void start() throws IOException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(pool, 1);
        server = AsynchronousServerSocketChannel.open(group);
        // 绑定端口
        server.bind(new InetSocketAddress(getConfig().getPort()));
        // 注册回调
        server.accept(
                this,
                new CompletionHandler<>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, AioServer attachment) {
                        // 处理业务
                        handleChannel(result);
                    }

                    @Override
                    public void failed(Throwable exc, AioServer attachment) {
                        log.error("IO失败:{}", exc.getMessage(), exc);
                    }
                });
    }

    private void handleChannel(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // attachment 传递参数
        client.read(
                buffer,
                buffer,
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {

                        attachment.flip(); // 移动 limit位置
                        read(attachment, result);
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        log.error("IO失败:{}", exc.getMessage(), exc);
                    }
                });
    }

    /**
     * 读取数据
     *
     * @param buffer
     */
    private void read(ByteBuffer buffer, int len) {

        try (ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), 0, len); ) {

            ObjectInput input = getConfig().getSerialization().deserialize(bis);
            RpcRequest request = input.readObject(RpcRequest.class);
            log.info("服务端收到信息:{}", request);
            Thread.sleep(1000);
        } catch (IOException e) {
            log.error("IO错误:{}", e.getMessage(), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向client发送数据
     *
     * @param clientChannel
     */
    private void write(AsynchronousSocketChannel clientChannel) {
        // 向client发送数据
        RpcResponse response = new RpcResponse();
        response.setCode(200);
        response.setBody(UUID.randomUUID().toString());
        log.debug("服务端发送数据:{}", response);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput output = getConfig().getSerialization().serialize(bos);
            output.writeObject(response);
            output.flushBuffer();
            byte[] bytes = bos.toByteArray();
            clientChannel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
