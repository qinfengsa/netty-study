package com.qinfengsa.common.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * RPC 响应信息
 *
 * @author qinfengsa
 * @date 2021/2/25 17:33
 */
@Data
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 6303753546288543069L;

    private int code;

    private String body;
}
