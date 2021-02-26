package com.qinfengsa.common.serialization;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Hessian 序列化
 *
 * @author qinfengsa
 * @date 2021/2/25 14:33
 */
@Slf4j
public class HessianSerialization implements Serialization {

    @Override
    public ObjectOutput serialize(OutputStream output) throws IOException {
        return new HessianObjectOutput(output);
    }

    @Override
    public ObjectInput deserialize(InputStream input) throws IOException {
        return new HessianObjectInput(input);
    }

    static class HessianObjectOutput implements ObjectOutput {
        private final Hessian2Output output;

        public HessianObjectOutput(OutputStream outputStream) {
            output = new Hessian2Output(outputStream);
        }

        @Override
        public void writeObject(Object obj) throws IOException {
            output.writeObject(obj);
        }

        @Override
        public void flushBuffer() throws IOException {
            output.flushBuffer();
        }
    }

    static class HessianObjectInput implements ObjectInput {

        private final Hessian2Input input;

        public HessianObjectInput(InputStream inputStream) {
            input = new Hessian2Input(inputStream);
        }

        @Override
        public <T> T readObject(Class<T> clazz) throws IOException {
            return (T) input.readObject(clazz);
        }
    }
}
