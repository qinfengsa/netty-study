package com.qinfengsa.server;

import java.io.IOException;
import lombok.Getter;

/**
 * 服务端 抽象类
 *
 * @author qinfengsa
 * @date 2021/2/24 16:31
 */
@Getter
public abstract class AbstractServer {

    // 端口号
    private final ServerConfig config;

    protected AbstractServer(ServerConfig config) {
        this.config = config;
    }

    // 启动
    public abstract void start() throws IOException;
}
