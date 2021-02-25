package com.qinfengsa.client;

import com.qinfengsa.common.serialization.Serialization;
import lombok.Builder;
import lombok.Getter;

/**
 * 客户端 配置信息
 *
 * @author qinfengsa
 * @date 2021/2/25 18:00
 */
@Getter
@Builder
public class ClientConfig {

    /* 地址 */
    private String addr;

    /* 端口号 */
    private int port;

    /** 序列化 */
    private final Serialization serialization;
}
