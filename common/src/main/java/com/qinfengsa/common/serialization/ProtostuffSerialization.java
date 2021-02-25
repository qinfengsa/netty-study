package com.qinfengsa.common.serialization;

import io.protostuff.GraphIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Protostuff 序列化
 *
 * @author qinfengsa
 * @date 2021/2/25 14:37
 */
public class ProtostuffSerialization implements Serialization {

    @Override
    public ObjectOutput serialize(OutputStream output) throws IOException {
        return new ProtostuffObjectOutput(output);
    }

    @Override
    public ObjectInput deserialize(InputStream input) throws IOException {
        return new ProtostuffObjectInput(input);
    }

    static class ProtostuffObjectOutput implements ObjectOutput {

        private LinkedBuffer buffer = LinkedBuffer.allocate();
        private final DataOutputStream dos;

        public ProtostuffObjectOutput(OutputStream outputStream) {
            dos = new DataOutputStream(outputStream);
        }

        @Override
        public void writeObject(Object obj) throws IOException {
            byte[] bytes;
            try {
                Schema schema = RuntimeSchema.getSchema(obj.getClass());
                bytes = GraphIOUtil.toByteArray(obj, schema, buffer);
            } finally {
                buffer.clear();
            }
            dos.writeInt(bytes.length);
            dos.write(bytes);
        }
    }

    static class ProtostuffObjectInput implements ObjectInput {

        private final DataInputStream dis;

        public ProtostuffObjectInput(InputStream inputStream) {
            dis = new DataInputStream(inputStream);
        }

        @Override
        public <T> T readObject(Class<T> clazz) throws IOException {

            int bytesLength = dis.readInt();

            if (bytesLength < 0) {
                throw new IOException();
            }

            byte[] bytes = new byte[bytesLength];
            dis.readFully(bytes, 0, bytesLength);

            Schema schema = RuntimeSchema.getSchema(clazz);
            Object result = schema.newMessage();
            GraphIOUtil.mergeFrom(bytes, result, schema);

            return (T) result;
        }
    }
}
