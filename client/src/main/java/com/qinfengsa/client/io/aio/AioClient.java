package com.qinfengsa.client.io.aio;

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
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * AIO 客户端
 *
 * @author qinfengsa
 * @date 2021/2/26 18:09
 */
@Slf4j
public class AioClient extends AbstractClient {

    private AsynchronousSocketChannel client;

    // 服务地址
    private final InetSocketAddress serverAddress;

    public AioClient(ClientConfig config) {
        super(config);
        serverAddress = new InetSocketAddress(config.getAddr(), config.getPort());
        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send() {

        client.connect(
                serverAddress,
                null,
                new CompletionHandler<>() {
                    @Override
                    public void completed(Void result, Object attachment) {

                        // 写数据
                        RpcRequest request = RpcRequest.getDefaultRequest(11);

                        log.debug("发送数据:{}", request);
                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                            ObjectOutput output = getConfig().getSerialization().serialize(bos);
                            output.writeObject(request);
                            output.flushBuffer();
                            byte[] bytes = bos.toByteArray();
                            client.write(ByteBuffer.wrap(bytes));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        log.error("IO失败:{}", exc.getMessage(), exc);
                    }
                });

        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(
                buffer,
                buffer,
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {

                        read(buffer, result);
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        log.error("IO失败:{}", exc.getMessage(), exc);
                    }
                });

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 读取数据
     *
     * @param buffer
     * @param len
     */
    private void read(ByteBuffer buffer, int len) {

        try (ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), 0, len)) {

            ObjectInput input = getConfig().getSerialization().deserialize(bis);
            RpcResponse response = input.readObject(RpcResponse.class);
            log.debug("收到服务器响应:{}", response);
            Thread.sleep(1000);
        } catch (IOException e) {
            log.error("IO错误:{}", e.getMessage(), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
