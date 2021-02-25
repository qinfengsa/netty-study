package com.qinfengsa.server.io.bio;

import com.qinfengsa.common.dto.RpcRequest;
import com.qinfengsa.common.dto.RpcResponse;
import com.qinfengsa.common.serialization.ObjectInput;
import com.qinfengsa.common.serialization.ObjectOutput;
import com.qinfengsa.common.serialization.Serialization;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/2/25 17:55
 */
@Slf4j
public class ClientHandler implements Runnable {
    private final Socket socket;

    private final Serialization serialization;

    public ClientHandler(Socket socket, Serialization serialization) {

        this.socket = socket;
        this.serialization = serialization;
    }

    @Override
    public void run() {
        try {
            ObjectInput input = serialization.deserialize(socket.getInputStream());
            RpcRequest request = input.readObject(RpcRequest.class);
            log.info("服务端收到信息:{}", request);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RpcResponse response = new RpcResponse();
            response.setCode(200);
            response.setBody(UUID.randomUUID().toString());
            log.debug("服务端发送数据:{}", response);
            ObjectOutput output = serialization.serialize(socket.getOutputStream());
            output.writeObject(response);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
