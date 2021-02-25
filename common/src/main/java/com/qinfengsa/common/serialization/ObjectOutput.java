package com.qinfengsa.common.serialization;

import java.io.IOException;

/**
 * @author qinfengsa
 * @date 2021/2/25 14:16
 */
public interface ObjectOutput {
    void writeObject(Object obj) throws IOException;
}
