package com.qinfengsa.client;

import lombok.Getter;

/**
 * 客户端 抽象
 *
 * @author qinfengsa
 * @date 2021/2/25 18:14
 */
@Getter
public abstract class AbstractClient {

    private final ClientConfig config;

    protected AbstractClient(ClientConfig config) {
        this.config = config;
    }

    /** 发送信息 */
    public abstract void send();
}
