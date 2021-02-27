package com.qinfengsa.client;

import com.qinfengsa.client.netty.NettyClient;
import com.qinfengsa.common.serialization.HessianSerialization;
import com.qinfengsa.common.serialization.Serialization;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/2/25 18:13
 */
@Slf4j
public class ClientMain {

    public static void main(String[] args) {
        Serialization serialization = new HessianSerialization();
        ClientConfig config =
                ClientConfig.builder()
                        .addr("localhost")
                        .port(8080)
                        .serialization(serialization)
                        .build();
        AbstractClient client = new NettyClient(config);
        client.send();
    }
}
