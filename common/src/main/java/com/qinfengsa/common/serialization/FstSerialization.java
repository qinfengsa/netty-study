package com.qinfengsa.common.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 * Fst 序列化
 *
 * @author qinfengsa
 * @date 2021/2/25 14:27
 */
public class FstSerialization implements Serialization {

    private final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Override
    public ObjectOutput serialize(OutputStream output) throws IOException {
        return new FstObjectOutput(output);
    }

    @Override
    public ObjectInput deserialize(InputStream input) throws IOException {
        return new FstObjectInput(input);
    }

    class FstObjectOutput implements ObjectOutput {

        private final FSTObjectOutput output;

        FstObjectOutput(OutputStream outputStream) {

            this.output = conf.getObjectOutput(outputStream);
        }

        @Override
        public void writeObject(Object obj) throws IOException {
            output.writeObject(obj);
        }
    }

    class FstObjectInput implements ObjectInput {

        private final FSTObjectInput input;

        FstObjectInput(InputStream inputStream) {
            this.input = conf.getObjectInput(inputStream);
        }

        @Override
        public <T> T readObject(Class<T> clazz) throws IOException {
            try {
                return (T) input.readObject(clazz);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}
