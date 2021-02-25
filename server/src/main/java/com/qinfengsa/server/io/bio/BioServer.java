package com.qinfengsa.server.io.bio;

import com.qinfengsa.common.serialization.Serialization;
import com.qinfengsa.server.AbstractServer;
import com.qinfengsa.server.ServerConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

/**
 * BIO 服务端
 *
 * @author qinfengsa
 * @date 2021/2/24 16:08
 */
@Slf4j
public class BioServer extends AbstractServer {

    private ServerSocket server;

    public BioServer(ServerConfig config) {
        super(config);
        try {
            server = new ServerSocket(config.getPort());
            log.debug("服务端启动-端口号:{}", config.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void start() throws IOException {
        Serialization serialization = getConfig().getSerialization();
        // 循环监听
        while (true) {
            Socket socket = server.accept();

            new ClientHandler(socket, serialization).run();
        }
    }
}
