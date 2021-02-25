package com.qinfengsa.common.serialization;

import java.io.IOException;

/**
 * @author qinfengsa
 * @date 2021/2/25 14:15
 */
public interface ObjectInput {

    <T> T readObject(Class<T> clazz) throws IOException;
}
