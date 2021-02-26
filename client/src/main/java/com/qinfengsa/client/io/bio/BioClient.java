package com.qinfengsa.client.io.bio;

import com.qinfengsa.client.AbstractClient;
import com.qinfengsa.client.ClientConfig;
import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import com.qinfengsa.common.serialization.ObjectInput;
import com.qinfengsa.common.serialization.ObjectOutput;
import com.qinfengsa.common.serialization.Serialization;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/2/24 15:34
 */
@Slf4j
public class BioClient extends AbstractClient {

    static ExecutorService pool = Executors.newFixedThreadPool(10);

    public BioClient(ClientConfig config) {
        super(config);
    }

    @Override
    public void send() {
        Serialization serialization = getConfig().getSerialization();
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            pool.execute(
                    () -> {
                        try {
                            Socket socket =
                                    new Socket(getConfig().getAddr(), getConfig().getPort());
                            RpcRequest request = RpcRequest.getDefaultRequest(idx);
                            ObjectOutput output = serialization.serialize(socket.getOutputStream());
                            log.debug("发送数据:{}", request);
                            output.writeObject(request);
                            output.flushBuffer();

                            Thread.sleep(1000);
                            ObjectInput input = serialization.deserialize(socket.getInputStream());
                            RpcResponse response = input.readObject(RpcResponse.class);
                            log.debug("收到服务器响应:{}", response);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
        }

        // pool.shutdown();
    }
}
