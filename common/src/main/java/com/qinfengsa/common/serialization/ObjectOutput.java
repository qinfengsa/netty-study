package com.qinfengsa.common.serialization;

import java.io.IOException;

/**
 * @author qinfengsa
 * @date 2021/2/25 14:16
 */
public interface ObjectOutput {
    /**
     * 写入数据
     *
     * @param obj
     * @throws IOException
     */
    void writeObject(Object obj) throws IOException;

    /**
     * Flush buffer.
     *
     * @throws IOException
     */
    void flushBuffer() throws IOException;
}
