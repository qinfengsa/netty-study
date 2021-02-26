package com.qinfengsa.common.dto;

import com.qinfengsa.common.entity.User;
import java.io.Serializable;
import java.util.UUID;
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

    private Object data;

    public static RpcRequest getDefaultRequest(int idx) {
        RpcRequest request = new RpcRequest();
        request.setBody(idx + ":" + UUID.randomUUID().toString());
        User user = new User();
        user.setId(idx);
        user.setName("qin" + idx);
        user.setAddr("Beijing");
        user.setAge(22);
        request.setData(user);
        return request;
    }
}
