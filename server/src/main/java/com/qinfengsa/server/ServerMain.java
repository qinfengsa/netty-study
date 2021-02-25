package com.qinfengsa.server;

import com.qinfengsa.common.serialization.HessianSerialization;
import com.qinfengsa.common.serialization.Serialization;
import com.qinfengsa.server.io.bio.BioServer;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qinfengsa
 * @date 2021/2/25 17:44
 */
@Slf4j
public class ServerMain {

    public static void main(String[] args) throws IOException {
        Serialization serialization = new HessianSerialization();
        ServerConfig config =
                ServerConfig.builder().port(8080).serialization(serialization).build();
        AbstractServer server = new BioServer(config);

        server.start();
    }
}
