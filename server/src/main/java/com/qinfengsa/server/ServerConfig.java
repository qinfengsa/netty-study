package com.qinfengsa.server;

import com.qinfengsa.common.serialization.Serialization;
import lombok.Builder;
import lombok.Getter;

/**
 * Server 配置信息
 *
 * @author qinfengsa
 * @date 2021/2/24 16:34
 */
@Getter
@Builder
public class ServerConfig {

    /** 端口号 */
    private final int port;

    /** 序列化 */
    private final Serialization serialization;
}
