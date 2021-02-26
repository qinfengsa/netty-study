package com.qinfengsa.server.io.bio;

import com.qinfengsa.common.serialization.Serialization;
import com.qinfengsa.server.AbstractServer;
import com.qinfengsa.server.ServerConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用线程池实现 伪异步IO
 *
 * @author qinfengsa
 * @date 2021/2/25 17:42
 */
@Slf4j
public class NewBioServer extends AbstractServer {

    private ServerSocket server;

    static ExecutorService pool = Executors.newFixedThreadPool(3);

    public NewBioServer(ServerConfig config) {
        super(config);
        try {
            server = new ServerSocket(config.getPort());
            log.debug("服务端启动-端口号:{}", config.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() throws IOException {
        Serialization serialization = getConfig().getSerialization();
        while (true) {
            Socket socket = server.accept();
            pool.execute(new ClientHandler(socket, serialization));
        }
    }
}
