package com.qinfengsa.common.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * RPC 请求信息
 *
 * @author qinfengsa
 * @date 2021/2/25 17:33
 */
@Data
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 7182021245193644002L;

    private int code;

    private String body;
}
