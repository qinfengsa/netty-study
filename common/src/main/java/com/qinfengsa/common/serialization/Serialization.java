package com.qinfengsa.common.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化接口
 *
 * @author qinfengsa
 * @date 2021/2/24 15:18
 */
public interface Serialization {

    // 序列化
    ObjectOutput serialize(OutputStream output) throws IOException;

    // 反序列化
    ObjectInput deserialize(InputStream input) throws IOException;
}
